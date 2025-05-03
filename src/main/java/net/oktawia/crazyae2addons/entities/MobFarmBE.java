package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.*;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.me.cluster.IAEMultiBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.oktawia.crazyae2addons.blocks.MobFarmWall;
import net.oktawia.crazyae2addons.clusters.MobFarmCluster;
import net.oktawia.crazyae2addons.clusters.MobFarmClusterCalculator;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.MobFarmMenu;
import net.oktawia.crazyae2addons.network.MobFarmClusterSyncRequestPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.StreamSupport;

public class MobFarmBE extends AENetworkBlockEntity implements
        IAEMultiBlock<MobFarmCluster>, MenuProvider, IUpgradeableObject {
    public MobFarmCluster cluster;
    public CompoundTag bufferTag;
    public final MobFarmClusterCalculator calculator;
    public boolean isCoreBlock = false;
    public CompoundTag waitingTag;
    public ArrayList<ItemStack> drops = new ArrayList<>();

    public MobFarmBE(BlockPos pos, BlockState state) {
        super(CrazyBlockEntityRegistrar.MOB_FARM_BE.get(), pos, state);
        this.calculator = new MobFarmClusterCalculator(this);
        this.getMainNode()
                .setFlags(GridFlags.MULTIBLOCK)
                .setIdlePowerUsage(0)
                .addService(IGridMultiblock.class, this::getMultiblockNodes)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.MOB_FARM_WALL_BLOCK.get().asItem())
                );
    }

    @Override
    public void disconnect(boolean update) {
        if (this.cluster != null) {
            for (var is : this.cluster.getInventory()) {this.drops.add(is);}
            for (var is : this.cluster.upgrades) {this.drops.add(is);}
            this.cluster.destroy();
            if (update && this.level != null) {
                this.setChanged();
                this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> cap,
            @Nullable Direction side
    ) {
        if (cap == Capabilities.IN_WORLD_GRID_NODE_HOST) {
            if (isCoreBlock && this.getGridNode(side) != null) {
                return LazyOptional.of(() -> (T) this).cast();
            } else {
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public IGridNode getGridNode(@Nullable Direction side) {
        return this.getMainNode().getNode();
    }

    public Iterator<IGridNode> getMultiblockNodes() {
        if (this.cluster == null) return Collections.emptyIterator();

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.cluster.getBlockEntities(), 0),false)
                .map(be -> be.getMainNode().getNode())
                .filter(Objects::nonNull)
                .iterator();
    }

    public void setCoreBlock(boolean core) {
        this.isCoreBlock = core;
    }

    @Override
    public MobFarmCluster getCluster() {
        return this.cluster;
    }

    public void setCluster(MobFarmCluster cluster) {
        if (this.cluster != null && this.cluster != cluster) {
            this.cluster.destroy();
        }
        this.cluster = cluster;
        if (this.bufferTag != null && this.cluster != null){
            this.cluster.inventory.readFromNBT(this.bufferTag, "clusterinventory");
            this.cluster.upgrades.readFromNBT(this.bufferTag, "clusterupgrades");
            this.bufferTag = null;
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction dir){
        return AECableType.DENSE_COVERED;
    }

    @Override
    public boolean isValid() {
        return this.level != null && !this.isRemoved();
    }

    public void updateBlockStateFormed(boolean formed) {
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof MobFarmWall) {
                level.setBlock(worldPosition, state.setValue(MobFarmWall.FORMED, formed), 3);
            }
        }
    }

    public static void tryFormCluster(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MobFarmBE mfpart) {
            mfpart.calculator.calculateMultiblock(level, pos);
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        if (this.getCluster() != null){
            this.cluster.damageBlocks = calculator.countBlocks(getLevel(), getBlockPos(), CrazyBlockRegistrar.MOB_FARM_DAMAGE_MODULE_BLOCK.get());
        }
        if (this.isCoreBlock && this.waitingTag != null) {
            this.cluster.readBlockEntitiesFromNBT(level, waitingTag);
            this.cluster.initNode();
            this.waitingTag = null;
        }
    }

    public static ServerLevel getLevelFromTag(CompoundTag tag) {
        if (!tag.contains("dimension")) return null;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        String dimId = tag.getString("dimension");
        if (dimId.isEmpty()) return null;

        ResourceLocation rl = new ResourceLocation(dimId);
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl);

        return server.getLevel(key);
    }

    @Override
    public void saveChanges(){
        super.saveChanges();
        if (this.level == null) {
            return;
        }
        this.getBlockEntity().setChanged();
        this.setChanged();
        this.markForUpdate();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (this.drops == null) return;
        for (var stack : this.drops) {
            var genericStack = GenericStack.unwrapItemStack(stack);
            if (genericStack != null) {
                genericStack.what().addDrops(
                        genericStack.amount(),
                        drops,
                        level,
                        pos);
            } else {
                drops.add(stack);
            }
        }
        this.drops = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (getLevel() == null) return;
        tag.putBoolean("coreblock", isCoreBlock);
        if (isCoreBlock && cluster != null) {
            CompoundTag clusterTag = new CompoundTag();
            cluster.writeToNBT(clusterTag);
            cluster.writeBlockEntitiesToNBT(clusterTag);
            clusterTag.putString("dimension", getLevel().dimension().location().toString());
            tag.put("cluster", clusterTag);
        }
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        if (!tag.getBoolean("coreblock") || !tag.contains("cluster")) return;
        if (this.cluster != null) return;
        CompoundTag clusterTag = tag.getCompound("cluster");
        this.level = getLevelFromTag(clusterTag);
        BlockPos min = BlockPos.of(clusterTag.getLong("clustermin"));
        BlockPos max = BlockPos.of(clusterTag.getLong("clustermax"));
        this.cluster = new MobFarmCluster(min, max);
        this.cluster.setCoreBlockEntity(this);
        this.cluster.readFromNBT(clusterTag);
        this.waitingTag = clusterTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (!tag.getBoolean("coreblock") || !tag.contains("cluster", Tag.TAG_COMPOUND)) return;
        CompoundTag clusterTag = tag.getCompound("cluster");
        BlockPos corePos = BlockPos.of(clusterTag.getLong("corepos"));
        BlockPos min = BlockPos.of(clusterTag.getLong("clustermin"));
        BlockPos max = BlockPos.of(clusterTag.getLong("clustermax"));
        this.cluster = new MobFarmCluster(min, max);
        this.cluster.setLevel(this.level);
        BlockEntity maybeCore = this.level.getBlockEntity(corePos);
        if (maybeCore instanceof MobFarmBE coreBE) {
            this.cluster.setCoreBlockEntity(coreBE);
        }
        this.cluster.readFromNBT(clusterTag);
        this.cluster.readBlockEntitiesFromNBT(this.level, clusterTag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (!isCoreBlock) {
            return null;
        }
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide) {
            NetworkHandler.INSTANCE.sendToServer(
                new MobFarmClusterSyncRequestPacket(this.worldPosition, level)
            );
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        if (this.cluster != null) {
            return this.cluster.getUpgrades();
        }
        return UpgradeInventories.empty();
    }

    public InternalInventory getInventory() {
        if (this.cluster != null) {
            return this.cluster.getInventory();
        }
        return InternalInventory.empty();
    }

    public void openMenu(Player player, MenuLocator locator) {
        if (this.getCluster() != null){
            MenuOpener.open(CrazyMenuRegistrar.MOB_FARM_MENU.get(), player, locator);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Mob Farm");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
        return new MobFarmMenu(id, inventory, this);
    }
}