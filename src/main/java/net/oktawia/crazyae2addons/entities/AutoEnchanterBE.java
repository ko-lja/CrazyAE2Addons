package net.oktawia.crazyae2addons.entities;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import dev.shadowsoffire.apotheosis.ench.table.RealEnchantmentHelper;
import mob_grinding_utils.MobGrindingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.AutoEnchanterMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.registries.tags.ITag;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.lang.Math;

import appeng.util.inv.CombinedInternalInventory;

import javax.annotation.Nonnull;


public class AutoEnchanterBE extends AENetworkInvBlockEntity implements IGridTickable, MenuProvider, IUpgradeableObject, IFluidHandler {

    public int selectedLevel = 1;

    public static final Set<TagKey<Fluid>> XP_FLUID_TAGS = Set.of(
            TagKey.create(Registries.FLUID, new ResourceLocation("forge", "experience")),
            TagKey.create(Registries.FLUID, new ResourceLocation("forge", "xpjuice"))
    );

    public final FluidTank fluidInv = new FluidTank(32000, fluidStack -> {
        for (TagKey<Fluid> tagKey : XP_FLUID_TAGS) {
            if (fluidStack.getFluid().builtInRegistryHolder().is(tagKey)) {
                return true;
            }
        }
        return false;
    });
    public final AppEngInternalInventory inputInv = new AppEngInternalInventory(this, 2, 64);
    public final AppEngInternalInventory outputInv = new AppEngInternalInventory(this, 1, 1);
    public final InternalInventory inv = new CombinedInternalInventory(this.inputInv, this.outputInv);

    private AutoEnchanterMenu menu;

    @Override
    public void onReady() {
        super.onReady();
        CompoundTag tag = this.getPersistentData();
        if (tag.contains("level")){
            this.selectedLevel = tag.getInt("level");
        }
    }

    @Override
    public int getTanks() {
        return this.fluidInv.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.fluidInv.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.fluidInv.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.fluidInv.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        var filledAmount = this.fluidInv.fill(resource, action);
        AutoEnchanterMenu curMenu = this.getMenu();
        if (curMenu != null){
            curMenu.fluidAmount = String.valueOf(this.fluidInv.getFluidAmount());
        }
        return filledAmount;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return this.fluidInv.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return this.fluidInv.drain(maxDrain, action);
    }

    public record EnchantCost(int xpLevels, int lapis) {
    }

    private Instant intervalStart;

    public AutoEnchanterBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(4)
                .addService(IGridTickable.class,this);
        intervalStart = Instant.now();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, true);
    }

    public static int getTotalXpForLevels(int level) {
        return level * level + 6 * level;
    }

    public static EnchantingStatRegistry.Stats gatherStats(Level world, BlockPos tablePos, int radius) {
        float eterna = 0;
        float maxEterna = 0;
        float quanta = 0;
        float arcana = 0;
        float rectification = 0;
        int clues = 0;

        for (BlockPos pos : BlockPos.betweenClosed(tablePos.offset(-radius, 0, -radius), tablePos.offset(radius, 1, radius))) {
            BlockState state = world.getBlockState(pos);
            eterna += EnchantingStatRegistry.getEterna(state, world, pos);
            maxEterna += EnchantingStatRegistry.getMaxEterna(state, world, pos);
            quanta += EnchantingStatRegistry.getQuanta(state, world, pos);
            arcana += EnchantingStatRegistry.getArcana(state, world, pos);
            rectification += EnchantingStatRegistry.getQuantaRectification(state, world, pos);
            clues += EnchantingStatRegistry.getBonusClues(state, world, pos);
        }

        return new EnchantingStatRegistry.Stats(maxEterna, eterna, quanta, arcana, rectification, clues);
    }


    public static ItemStack enchantWithApotheosis(ItemStack item, int enchantLevel, EnchantingStatRegistry.Stats stats, ServerLevel level) {
        RandomSource random = level.getRandom();

        var enchantments = RealEnchantmentHelper.selectEnchantment(
                random,
                item,
                enchantLevel,
                stats.quanta(),
                stats.arcana(),
                stats.rectification(),
                false,
                Set.of()
        );

        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantments.forEach(ei -> EnchantedBookItem.addEnchantment(enchantedBook, ei));

        return enchantedBook;
    }

    public static EnchantCost calculateEnchantCost(int enchantOption, ItemStack what, EnchantingStatRegistry.Stats stats) {
        RandomSource rand = RandomSource.create();
        int xpCost = RealEnchantmentHelper.getEnchantmentCost(rand, enchantOption, stats.eterna(), what);
        xpCost = Math.max(getTotalXpForLevels(xpCost), 16);
        int lapisCost = enchantOption + 1;
        return new EnchantCost(xpCost, lapisCost);
    }

    public AutoEnchanterMenu getMenu(){
        return this.menu;
    }

    public void setMenu(AutoEnchanterMenu menu){
        this.menu = menu;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        BlockPos target = getBlockEntity().getBlockPos().relative(getTop());
        BlockState targetState = null;
        try {
            targetState = getLevel().getBlockState(target);
        } catch (Exception ignored) {
            return TickRateModulation.IDLE;
        }
        if (targetState.is(Blocks.ENCHANTING_TABLE) && Instant.now().getEpochSecond() - intervalStart.getEpochSecond() > 10) {

            double extractedPower;
            try {
                 extractedPower = getMainNode().getGrid().getEnergyService().extractAEPower(128, Actionable.MODULATE, PowerMultiplier.CONFIG);
            } catch (Exception ignored) {
                return TickRateModulation.IDLE;
            }

            if (extractedPower < 128){
                return TickRateModulation.IDLE;
            }

            EnchantingStatRegistry.Stats stats = gatherStats(getLevel(), target, 3);

            ItemStack book = new ItemStack(Items.BOOK);

            int enchantLevel = Math.min((int)stats.eterna(), (int)stats.maxEterna());
            enchantLevel = Math.min(enchantLevel, 100);

            if (selectedLevel == 1){
                enchantLevel = enchantLevel / 3;
            } else if (selectedLevel == 2) {
                enchantLevel = enchantLevel * 2 / 3;
            }

            EnchantCost cost = calculateEnchantCost(2, book, stats);

            if (!outputInv.isEmpty()){
                return TickRateModulation.IDLE;
            }

            if (inv.getStackInSlot(0).isEmpty() || inv.getStackInSlot(0).getItem() != Items.BOOK)
                return TickRateModulation.IDLE;

            if (inv.getStackInSlot(1).getCount() < cost.lapis || inv.getStackInSlot(1).getItem() != Items.LAPIS_LAZULI)
                return TickRateModulation.IDLE;

            int requiredXpFluid = cost.xpLevels;
            FluidStack extracted = fluidInv.drain(requiredXpFluid, IFluidHandler.FluidAction.SIMULATE);
            if (extracted.getAmount() < requiredXpFluid)
                return TickRateModulation.IDLE;

            inv.extractItem(0, 1, false);
            inv.extractItem(1, cost.lapis, false);
            fluidInv.setFluid(new FluidStack(fluidInv.getFluid(), fluidInv.getFluidAmount() - requiredXpFluid));

            book = enchantWithApotheosis(
                    book,
                    enchantLevel,
                    stats,
                    (ServerLevel) getLevel()
            );

            outputInv.setItemDirect(0, book);

            intervalStart = Instant.now();
            if (getMenu() != null){
                this.getMenu().fluidAmount = String.valueOf(this.fluidInv.getFluidAmount());
            }
        }
        return TickRateModulation.IDLE;
    }



    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player pPlayer) {
        return new AutoEnchanterMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.AUTO_ENCHANTER_MENU, player, locator);
    }

    public static boolean validXpFluidIsPresent() {
        return XP_FLUID_TAGS
                .stream()
                .anyMatch(tag -> !getFluidTag(tag).isEmpty());
    }

    @Nonnull
    private static ITag<Fluid> getFluidTag(TagKey<Fluid> tag) {
        return Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                .getTag(tag);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (validXpFluidIsPresent() && capability == ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.of(() -> this).cast();
        }

        return super.getCapability(capability, side);
    }
}

