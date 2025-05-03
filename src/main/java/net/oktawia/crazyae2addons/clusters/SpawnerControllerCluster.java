package net.oktawia.crazyae2addons.clusters;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.*;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.me.cluster.IAECluster;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.entities.SpawnerControllerBE;
import net.oktawia.crazyae2addons.mobstorage.MobKey;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.network.SpawnerControllerClusterDeletePacket;
import org.jline.utils.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SpawnerControllerCluster implements IAECluster, IUpgradeableObject, InternalInventoryHost, IGridTickable {

    public static final Integer INV_SIZE = 0;
    public static final Integer UPGRADE_INV_SIZE = 4;
    public static final Integer CONFIG_INV_SIZE = 0;
    public final AppEngInternalInventory inventory = new AppEngInternalInventory(this, INV_SIZE, 1);
    public final IUpgradeInventory upgrades = UpgradeInventories.forMachine(
            CrazyBlockRegistrar.SPAWNER_CONTROLLER_WALL_BLOCK.get(), UPGRADE_INV_SIZE, this::saveChanges);
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private Level level;
    private final List<SpawnerControllerBE> blockEntities = new ArrayList<>();
    public boolean isDestroyed = true;
    private SpawnerControllerBE core;
    private final ConfigInventory configInventory = ConfigInventory.configTypes(what -> (what instanceof MobKey), CONFIG_INV_SIZE, this::saveChanges);
    public IManagedGridNode gridNode;
    public static final ClusterPattern STRUCTURE_PATTERN =
            new ClusterPattern(new ResourceLocation("crazyae2addons", "spawner_controller"));
    public IGridConnection conn;
    public boolean disabledSpawners = false;

    public SpawnerControllerCluster(BlockPos minPos, BlockPos maxPos) {
        this.minPos = minPos.immutable();
        this.maxPos = maxPos.immutable();
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putLong("clustermin", this.getBoundsMin().asLong());
        tag.putLong("clustermax", this.getBoundsMax().asLong());
        this.inventory.writeToNBT(tag, "clusterinventory");
        this.upgrades.writeToNBT(tag, "clusterupgrades");
    }

    public void readFromNBT(CompoundTag tag) {
        this.inventory.readFromNBT(tag, "clusterinventory");
        this.upgrades.readFromNBT(tag, "clusterupgrades");
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public BlockPos getBoundsMin() {
        return minPos;
    }

    @Override
    public BlockPos getBoundsMax() {
        return maxPos;
    }

    @Override
    public void destroy() {
        if (isDestroyed) return;
        this.isDestroyed = true;
        this.enableAllSpawnersInStructure();
        if (this.gridNode != null) {
            this.gridNode.destroy();
            this.gridNode = null;
        }
        if (core != null) {
            core.cluster = null;
            core.updateBlockStateFormed(false);
            core.saveChanges();
            core.getLevel().updateNeighborsAt(core.getBlockPos(), core.getBlockState().getBlock());
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new SpawnerControllerClusterDeletePacket(core.getBlockPos())
            );
        }
        for (SpawnerControllerBE be : new ArrayList<>(this.blockEntities)) {
            be.cluster = null;
            be.updateBlockStateFormed(false);
            be.saveChanges();
            be.getLevel().updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new SpawnerControllerClusterDeletePacket(
                    be.getBlockPos()
                )
            );
        }
        blockEntities.clear();
    }

    public IGridNode getNode() {
        return this.gridNode.getNode();
    }

    public void initNode() {
        var node = GridHelper.createManagedNode(this, (x, y)->{})
            .setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(16)
            .addService(IGridTickable.class, this)
            .setExposedOnSides(Set.of(Direction.values()))
            .setVisualRepresentation(new ItemStack(CrazyBlockRegistrar.SPAWNER_CONTROLLER_WALL_BLOCK.get().asItem()));
        node.create(core.getLevel(), core.getBlockPos());
        this.gridNode = node;
        if (node.getNode() != null && getCoreBlockEntity().getGridNode() != null) {
            this.conn = GridHelper.createConnection(node.getNode(), getCoreBlockEntity().getGridNode());
        }
    }

    @Override
    public void updateStatus(boolean formed) {
        for (SpawnerControllerBE be : new ArrayList<>(this.blockEntities)) {
            be.updateBlockStateFormed(true);
            be.setCluster(this);
            be.getLevel().updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
        }
    }

    public void writeBlockEntitiesToNBT(CompoundTag tag) {
        ListTag list = new ListTag();

        for (SpawnerControllerBE be : blockEntities) {
            CompoundTag beTag = new CompoundTag();
            beTag.putInt("x", be.getBlockPos().getX());
            beTag.putInt("y", be.getBlockPos().getY());
            beTag.putInt("z", be.getBlockPos().getZ());
            list.add(beTag);
        }

        tag.put("BlockEntities", list);
    }

    public void readBlockEntitiesFromNBT(Level level, CompoundTag tag) {
        this.blockEntities.clear();

        if (tag.contains("BlockEntities", Tag.TAG_LIST)) {
            ListTag list = tag.getList("BlockEntities", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag beTag = (CompoundTag) t;
                BlockPos pos = new BlockPos(
                        beTag.getInt("x"),
                        beTag.getInt("y"),
                        beTag.getInt("z")
                );
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SpawnerControllerBE spawner) {
                    spawner.setCluster(this);
                    this.blockEntities.add(spawner);
                }
            }
        }
    }

    @Override
    public Iterator<SpawnerControllerBE> getBlockEntities() {
        return blockEntities.iterator();
    }

    public void addBlockEntity(SpawnerControllerBE be) {
        this.blockEntities.add(be);
        be.cluster = this;
    }

    public void disableSpawner(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SpawnerBlockEntity spawner) {
            BaseSpawner logic = spawner.getSpawner();
            try {
                Field field = BaseSpawner.class.getDeclaredField("requiredPlayerRange");
                field.setAccessible(true);
                field.setInt(logic, 0);
                spawner.setChanged();
                this.disabledSpawners = true;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
    }

    public void enableSpawner(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SpawnerBlockEntity spawner) {
            BaseSpawner logic = spawner.getSpawner();
            try {
                Field field = BaseSpawner.class.getDeclaredField("requiredPlayerRange");
                field.setAccessible(true);
                field.setInt(logic, 16);
                spawner.setChanged();
                this.disabledSpawners = false;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
    }

    public void enableAllSpawnersInStructure() {
        if (this.core == null || this.core.getLevel() == null || !this.disabledSpawners) return;

        BlockPos origin = ClusterPattern.findOrigin(this.core.getLevel(), core.getBlockPos(), STRUCTURE_PATTERN.getAllValidBlocks());

        for (ClusterPattern.Rotation rot : ClusterPattern.Rotation.values()) {
            if (STRUCTURE_PATTERN.matchesWithRotation(this.core.getLevel(), origin, rot)) {
                List<BlockPos> spawnerPositions = STRUCTURE_PATTERN.getSymbolPositions(this.core.getLevel(), origin, rot, '.');
                for (BlockPos pos : spawnerPositions) {
                    enableSpawner(this.core.getLevel(), pos);
                }
                break;
            }
        }
    }

    public void disableAllSpawnersInStructure() {
        if (this.core == null || this.core.getLevel() == null || this.disabledSpawners) return;

        BlockPos origin = ClusterPattern.findOrigin(this.core.getLevel(), core.getBlockPos(), STRUCTURE_PATTERN.getAllValidBlocks());

        for (ClusterPattern.Rotation rot : ClusterPattern.Rotation.values()) {
            if (STRUCTURE_PATTERN.matchesWithRotation(this.core.getLevel(), origin, rot)) {
                List<BlockPos> spawnerPositions = STRUCTURE_PATTERN.getSymbolPositions(this.core.getLevel(), origin, rot, '.');
                for (BlockPos pos : spawnerPositions) {
                    disableSpawner(this.core.getLevel(), pos);
                }
                break;
            }
        }
    }

    public void done() {
        this.isDestroyed = false;
        updateStatus(true);
        var core = this.getCoreBlockEntity();
        this.initNode();
        if (core == null) return;
        this.saveChanges();
    }

    public void setLevel(Level level) {
        this.level = level == null ? this.level : level;
    }

    public boolean isClientSide() {
        return level == null || level.isClientSide;
    }

    @Override
    public void saveChanges() {
        this.getCoreBlockEntity().saveChanges();
    }

    public void setCoreBlockEntity(SpawnerControllerBE be){
        this.core = be;
        be.cluster = this;
    }

    public SpawnerControllerBE getCoreBlockEntity() {
        if (this.core != null) {
            return this.core;
        }
        boolean first = true;
        for (SpawnerControllerBE be : this.blockEntities) {
            if (first) {
                this.core = be;
                be.setCoreBlock(true);
                first = false;
            } else {
                be.setCoreBlock(false);
            }
        }
        return this.core;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.saveChanges();
    }

    public ConfigInventory getConfigInventory() {
        return this.configInventory;
    }

    public AppEngInternalInventory getInventory() {
        return this.inventory;
    }

    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public List<SpawnerBlockEntity> getSpawnersInStructure() {
        List<SpawnerBlockEntity> result = new ArrayList<>();

        if (this.core.getLevel() == null || this.core == null) return result;

        BlockPos origin = ClusterPattern.findOrigin(this.core.getLevel(), core.getBlockPos(), STRUCTURE_PATTERN.getAllValidBlocks());

        for (ClusterPattern.Rotation rot : ClusterPattern.Rotation.values()) {
            if (STRUCTURE_PATTERN.matchesWithRotation(this.core.getLevel(), origin, rot)) {
                List<BlockPos> spawnerPositions = STRUCTURE_PATTERN.getSymbolPositions(this.core.getLevel(), origin, rot, '.');
                for (BlockPos pos : spawnerPositions) {
                    BlockEntity be = this.core.getLevel().getBlockEntity(pos);
                    if (be instanceof SpawnerBlockEntity spawner) {
                        result.add(spawner);
                    }
                }
                break;
            }
        }

        return result;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    public static EntityType<?> getEntityTypeFromSpawner(SpawnerBlockEntity spawner) {
        BaseSpawner logic = spawner.getSpawner();
        try {
            Field nextSpawnDataField = BaseSpawner.class.getDeclaredField("nextSpawnData");
            nextSpawnDataField.setAccessible(true);
            Object spawnData = nextSpawnDataField.get(logic);

            if (spawnData != null) {
                Method entityToSpawnMethod = spawnData.getClass().getDeclaredMethod("entityToSpawn");
                entityToSpawnMethod.setAccessible(true);
                CompoundTag tag = (CompoundTag) entityToSpawnMethod.invoke(spawnData);

                if (tag.contains("id")) {
                    return EntityType.byString(tag.getString("id")).orElse(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!CrazyConfig.COMMON.enablePeacefullSpawner.get() && (this.core != null && this.core.getLevel() != null && this.core.getLevel().getDifficulty() == Difficulty.PEACEFUL)) {
            return TickRateModulation.IDLE;
        }
        this.disableAllSpawnersInStructure();
        var spawners = getSpawnersInStructure();
        if (!spawners.isEmpty()){
            var spawner = spawners.get(0);
            var type = getEntityTypeFromSpawner(spawner);
            if (type != null){
                MEStorage inv = getNode().getGrid().getStorageService().getInventory();
                IEnergyService eng = getNode().getGrid().getEnergyService();
                long amt = this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD) + 1;
                StorageHelper.poweredInsert(eng, inv, MobKey.of(type), amt, IActionSource.ofMachine(getCoreBlockEntity()), Actionable.MODULATE);
            }
        }
        return TickRateModulation.IDLE;
    }
}