package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import mezz.jei.common.Internal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.oktawia.crazyae2addons.blocks.MobFarmController;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.items.XpShardItem;
import net.oktawia.crazyae2addons.menus.MobFarmControllerMenu;
import net.oktawia.crazyae2addons.mobstorage.MobKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobFarmControllerBE extends AENetworkInvBlockEntity implements MenuProvider, IGridTickable, IUpgradeableObject {

    public boolean formed = false;
    public AEKeyFilter filter = what -> (what instanceof MobKey);
    public ConfigInventory config = ConfigInventory.configTypes(filter, 3, this::saveChanges);
    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 1, 1);
    public Integer damageBlocks = 0;
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(CrazyBlockRegistrar.MOB_FARM_CONTROLLER_BLOCK.get(), 5, this::saveChanges);
    public ServerPlayer sp;

    public MobFarmControllerBE(BlockPos pos, BlockState state) {
        super(CrazyBlockEntityRegistrar.MOB_FARM_CONTROLLER_BE.get(), pos, state);
        this.getMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setIdlePowerUsage(4)
            .addService(IGridTickable.class, this);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.config.writeToChildTag(data, "config");
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) { this.markForUpdate(); }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.config.readFromChildTag(data, "config");
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    public void openMenu(Player player, MenuLocator locator) {
        if (this.formed){
            MenuOpener.open(CrazyMenuRegistrar.MOB_FARM_CONTROLLER_MENU.get(), player, locator);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Multiblock Cluster");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        if (this.formed) {
            return new MobFarmControllerMenu(id, playerInventory, this);
        }
        return null;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 5, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (isClientSide()) {
            return TickRateModulation.IDLE;
        }

        boolean isValid = false;

        if (this.getBlockState().getBlock() instanceof MobFarmController MFC) {
            isValid = MFC.tryFormCluster(getLevel(), getBlockPos());

            if (isValid) {
                this.damageBlocks = MFC.countBlocksInCluster(getLevel(), getBlockPos(), CrazyBlockRegistrar.MOB_FARM_DAMAGE_MODULE_BLOCK.get());
                this.doWork();
            }
        }

        if (this.formed) {
            if (!isValid) {
                this.formed = false;
            }
        } else {
            if (this.getBlockState().getBlock() instanceof MobFarmController MFC) {
                this.formed = MFC.tryFormCluster(getLevel(), getBlockPos());
            }
        }

        return TickRateModulation.IDLE;
    }

    public int getSpeed() {
        if (this.damageBlocks <= 0) {
            return 0;
        }
        int upgradeCount = this.getUpgrades().getInstalledUpgrades(AEItems.SPEED_CARD);
        int maxSpeed = switch (upgradeCount) {
            case 0 -> 16;
            case 1 -> 28;
            case 2 -> 40;
            case 3 -> 52;
            case 4 -> 64;
            default -> 16;
        };
        double ratio = this.damageBlocks / 16.0;
        return (int) Math.round(maxSpeed * ratio / 4);
    }

    public EntityDropResult getDropsAndExp(ServerLevel level, EntityType<?> type) {
        try {
            Entity entity = type.create(level);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return new EntityDropResult(List.of(), 0);
            }

            if (this.sp == null) {
                this.sp = level.getServer().getPlayerList().getPlayers().stream().findFirst().orElse(null);
            }

            ItemStack setDamager = this.inv.getStackInSlot(0);
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
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, this.sp)
                    .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.sp)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(this.sp))
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
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack is : this.getUpgrades()){
            drops.add(is);
        }
    }

    public record EntityDropResult(List<ItemStack> drops, int experience) {}

    public void doWork(){
        MEStorage inv = getGridNode().getGrid().getStorageService().getInventory();
        IEnergyService eng = getGridNode().getGrid().getEnergyService();
        int i;
        for (i = 0; i < getSpeed();){
            AEKey key = this.config.getKey(i % 3);
            if (key == null){
                i++;
                continue;
            }
            long extracted = StorageHelper.poweredExtraction(eng, inv, key, 1, IActionSource.ofMachine(this), Actionable.MODULATE);
            if (extracted <= 0){
                i++;
                continue;
            }
            EntityType<?> entityType = ((MobKey) key).getEntityType();
            EntityDropResult result = getDropsAndExp(getLevel().getServer().getLevel(getLevel().dimension()), entityType);
            for (ItemStack is : result.drops){
                StorageHelper.poweredInsert(eng, inv, AEItemKey.of(is.getItem()), is.getCount(), IActionSource.ofMachine(this), Actionable.MODULATE);
            }
            StorageHelper.poweredInsert(eng, inv, AEItemKey.of(CrazyItemRegistrar.XP_SHARD_ITEM.get()),
                    result.experience / XpShardItem.XP_VAL, IActionSource.ofMachine(this), Actionable.MODULATE);
            i++;
        }
    }
}

