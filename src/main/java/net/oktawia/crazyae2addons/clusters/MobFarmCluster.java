package net.oktawia.crazyae2addons.clusters;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.me.Grid;
import appeng.me.cluster.IAECluster;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.entities.MobFarmBE;
import net.oktawia.crazyae2addons.items.XpShardItem;
import net.oktawia.crazyae2addons.mobstorage.MobKey;
import net.oktawia.crazyae2addons.network.MobFarmClusterDeletePacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;

import java.util.*;

public class MobFarmCluster implements IAECluster, IUpgradeableObject, InternalInventoryHost, IGridTickable {

    public static final Integer INV_SIZE = 1;
    public static final Integer UPGRADE_INV_SIZE = 5;
    public static final Integer CONFIG_INV_SIZE = 3;
    public final AppEngInternalInventory inventory = new AppEngInternalInventory(this, INV_SIZE, 1);
    public final IUpgradeInventory upgrades = UpgradeInventories.forMachine(
            CrazyBlockRegistrar.MOB_FARM_WALL_BLOCK.get(), UPGRADE_INV_SIZE, this::saveChanges);
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private Level level;
    private final List<MobFarmBE> blockEntities = new ArrayList<>();
    public Integer damageBlocks = 0;
    public boolean isDestroyed = true;
    private MobFarmBE core;
    private final ConfigInventory configInventory = ConfigInventory.configTypes(what -> (what instanceof MobKey), CONFIG_INV_SIZE, this::saveChanges);
    public IManagedGridNode gridNode;
    public FakePlayer fakePlayer;

    public MobFarmCluster(BlockPos minPos, BlockPos maxPos) {
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

        for (MobFarmBE be : new ArrayList<>(this.blockEntities)) {
            be.cluster = null;
            be.updateBlockStateFormed(false);
            be.saveChanges();
            be.getLevel().updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new MobFarmClusterDeletePacket(
                    be.getBlockPos()
                )
            );
        }
        blockEntities.clear();
        if (this.gridNode != null) {
            this.gridNode.destroy();
            this.gridNode = null;
        }
    }

    public IGridNode getNode() {
        var node = this.gridNode;
        if (node.getNode() == null){
        }
        return node.getNode();
    }

    public void initNode() {
        var node = GridHelper.createManagedNode(this, (x, y)->{})
            .setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(16)
            .addService(IGridTickable.class, this)
            .setExposedOnSides(Set.of(Direction.values()))
            .setVisualRepresentation(new ItemStack(CrazyBlockRegistrar.MOB_FARM_WALL_BLOCK.get().asItem()));
        node.create(core.getLevel(), core.getBlockPos());
        this.gridNode = node;
        if (node.getNode() != null && getCoreBlockEntity().getGridNode() != null) {
            GridHelper.createConnection(node.getNode(), getCoreBlockEntity().getGridNode());
        }
    }

    @Override
    public void updateStatus(boolean formed) {
        for (MobFarmBE be : new ArrayList<>(this.blockEntities)) {
            be.updateBlockStateFormed(true);
            be.setCluster(this);
            be.getLevel().updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
        }
    }

    public void writeBlockEntitiesToNBT(CompoundTag tag) {
        ListTag list = new ListTag();

        for (MobFarmBE be : blockEntities) {
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
                if (be instanceof MobFarmBE mob) {
                    mob.setCluster(this);
                    this.blockEntities.add(mob);
                }
            }
        }
    }

    @Override
    public Iterator<MobFarmBE> getBlockEntities() {
        return blockEntities.iterator();
    }

    public void addBlockEntity(MobFarmBE be) {
        this.blockEntities.add(be);
        be.setCluster(this);
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

    public void setCoreBlockEntity(MobFarmBE be){
        this.core = be;
        be.setCoreBlock(true);
    }

    public MobFarmBE getCoreBlockEntity() {
        if (this.core != null) {
            return this.core;
        }
        boolean first = true;
        for (MobFarmBE be : this.blockEntities) {
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

    public int getSpeed() {
        if (this.damageBlocks <= 0) {
            return 0;
        }
        int upgradeCount = this.getUpgrades().getInstalledUpgrades(AEItems.SPEED_CARD);
        int maxSpeed = switch (upgradeCount) {
            case 1 -> 28;
            case 2 -> 40;
            case 3 -> 52;
            case 4 -> 64;
            default -> 16;
        };
        double ratio = this.damageBlocks / 16.0;
        return (int) Math.round(maxSpeed * ratio);
    }

    public record EntityDropResult(List<ItemStack> drops, int experience) {}

    public EntityDropResult getDropsAndExp(ServerLevel level, EntityType<?> type) {
        try {
            Entity entity = type.create(level);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return new EntityDropResult(List.of(), 0);
            }

            if (this.fakePlayer == null){
                this.fakePlayer = FakePlayerFactory.get(Objects.requireNonNull(level.getServer()).getLevel(level.dimension()),
                        new GameProfile(UUID.randomUUID(), "[CrazyAE2Addons]"));
            }

            ItemStack setDamager = this.getInventory().getStackInSlot(0);
            ItemStack damager;
            if (setDamager == null || setDamager.isEmpty()) {
                damager = new ItemStack(Items.STICK);
            } else {
                damager = setDamager.copy();
            }

            int lootingLevel = getInstalledUpgrades(CrazyItemRegistrar.LOOTING_UPGRADE_CARD.get());
            Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(damager));
            int finalLootingLvl = lootingLevel + enchants.getOrDefault(Enchantments.MOB_LOOTING, 0);
            enchants.put(Enchantments.MOB_LOOTING, finalLootingLvl);
            EnchantmentHelper.setEnchantments(enchants, damager);

            int exp = livingEntity.getExperienceReward();

            ResourceLocation lootTableId = type.getDefaultLootTable();
            LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);

            LootParams lootParams = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, this.fakePlayer)
                    .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.fakePlayer)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(this.fakePlayer))
                    .withParameter(LootContextParams.TOOL, damager)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(livingEntity.blockPosition()))
                    .create(LootContextParamSets.ENTITY);

            int rolls = 1 + finalLootingLvl;
            Map<Item, Integer> itemCounts = new HashMap<>();

            for (int i = 0; i < rolls; i++) {
                List<ItemStack> drops = lootTable.getRandomItems(lootParams);
                for (ItemStack stack : drops) {
                    if (!stack.isEmpty() && !stack.hasTag() && stack.getMaxStackSize() != 1) {
                        itemCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    }
                }
            }

            List<ItemStack> allDrops = new ArrayList<>();
            for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
                allDrops.add(new ItemStack(entry.getKey(), entry.getValue()));
            }

            return new EntityDropResult(allDrops, exp);

        } catch (Exception e) {
            return new EntityDropResult(List.of(), 0);
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        level = core.getLevel();
        if (getNode() == null || getNode().getGrid() == null || level == null || level.getServer() == null || getNode().getUsedChannels() <= 0) return TickRateModulation.IDLE;
        MEStorage inv = getNode().getGrid().getStorageService().getInventory();
        IEnergyService eng = getNode().getGrid().getEnergyService();
        int speed = Math.max(getSpeed(), 1);
        int i;
        for (i = 0; i < speed;){
            AEKey key = this.getConfigInventory().getKey(i % MobFarmCluster.CONFIG_INV_SIZE);
            if (key == null){
                i++;
                continue;
            }
            long extracted = StorageHelper.poweredExtraction(eng, inv, key, 1, IActionSource.ofMachine(getCoreBlockEntity()), Actionable.MODULATE);
            if (extracted <= 0){
                i++;
                continue;
            }
            EntityType<?> entityType = ((MobKey) key).getEntityType();
            EntityDropResult result = getDropsAndExp(level.getServer().getLevel(level.dimension()), entityType);
            for (ItemStack is : result.drops){
                StorageHelper.poweredInsert(eng, inv, AEItemKey.of(is.getItem()), is.getCount(), IActionSource.ofMachine(getCoreBlockEntity()), Actionable.MODULATE);
            }
            StorageHelper.poweredInsert(eng, inv, AEItemKey.of(CrazyItemRegistrar.XP_SHARD_ITEM.get()),
                    Math.max((result.experience / XpShardItem.XP_VAL), 1), IActionSource.ofMachine(getCoreBlockEntity()), Actionable.MODULATE);
            i++;
        }
        return TickRateModulation.IDLE;
    }
}