package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.DataProcessorMenu;
import net.oktawia.crazyae2addons.menus.IsolatedDataProcessorMenu;
import net.oktawia.crazyae2addons.misc.LogicSetting;
import net.oktawia.crazyae2addons.misc.NBTContainer;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class IsolatedDataProcessorBE extends AENetworkInvBlockEntity implements MenuProvider, IUpgradeableObject, IGridTickable {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 9);
    public IsolatedDataProcessorMenu menu;
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(Blocks.ISOLATED_DATA_PROCESSOR_BLOCK, 0, this::saveChanges);
    public String identifier;
    public Integer submenuNum;
    public NBTContainer settings = new NBTContainer();
    public String in = "";
    public String in1 = "0";
    public String in2 = "";
    public String out = "";
    public int i = 0;
    public int x = 0;
    public int y = 0;
    public int l0 = 0;
    public int l1 = 0;
    public int l2 = 0;
    public int l3 = 0;

    public IsolatedDataProcessorBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(4)
                .addService(IGridTickable.class, this);
        for (int i = 0; i < inv.size(); i++) {
            this.inv.setMaxStackSize(i, 1);
            settings.set(String.valueOf(i), new LogicSetting());
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.markForUpdate();
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

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if(data.contains("identifier")){
            this.identifier = data.getString("identifier");
        }
        this.settings.deserialize(data.getByteArray("settings"));
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        if(this.identifier == null){
            this.identifier = randomHexId();
        }
        data.putString("identifier", this.identifier);
        data.putByteArray("settings", this.settings.serialize(true));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new IsolatedDataProcessorMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.ISOLATED_DATA_PROCESSOR_MENU, player, locator);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void setMenu(IsolatedDataProcessorMenu menu) {
        this.menu = menu;
    }

    public IsolatedDataProcessorMenu getMenu() {
        return this.menu;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, false);
    }
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (i >= getInternalInventory().size()) {
            i = 0;
            return TickRateModulation.IDLE;
        }
        var itemStack = getInternalInventory().getStackInSlot(i);
        if (itemStack.isEmpty()) {
            i = 0;
            return TickRateModulation.IDLE;
        }

        LogicSetting setting = (LogicSetting) this.settings.get(String.valueOf(i));
        in1 = (i != 0) ? setting.in1 : "0";
        in2 = setting.in2;
        out = setting.out;

        int x = evaluateInput(in1);
        int y = evaluateInput(in2);

        TickRateModulation modulation = processConditionalCards(itemStack, x, y);
        if (modulation != null) return modulation;

        int temp = computeTempValue(itemStack, x, y);
        if (temp == Integer.MIN_VALUE) {
            i++;
            return TickRateModulation.IDLE;
        }

        applyOutput(out, temp);
        i++;
        return TickRateModulation.IDLE;
    }

    private int evaluateInput(String input) {
        if (input.startsWith("&&")) {
            return switch (input) {
                case "&&0" -> l0;
                case "&&1" -> l1;
                case "&&2" -> l2;
                case "&&3" -> l3;
                default -> 0;
            };
        } else if (input.startsWith("&")) {
            MEDataControllerBE controller = getGridController();
            return (controller != null) ? controller.getVariable(input.replace("&", "")) : 0;
        } else {
            try {
                return Integer.parseInt(input);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private TickRateModulation processConditionalCards(ItemStack itemStack, int x, int y) {
        if (itemStack.is(Items.HIT_CARD.asItem())) {
            i = (x > 0) ? y : i + 1;
            return TickRateModulation.IDLE;
        } else if (itemStack.is(Items.HIF_CARD.asItem())) {
            i = (x <= 0) ? y : i + 1;
            return TickRateModulation.IDLE;
        }
        return null;
    }

    private int computeTempValue(ItemStack itemStack, int x, int y) {
        if (itemStack.is(Items.ADD_CARD.asItem())) return x + y;
        if (itemStack.is(Items.SUB_CARD.asItem())) return x - y;
        if (itemStack.is(Items.MUL_CARD.asItem())) return x * y;
        if (itemStack.is(Items.DIV_CARD.asItem()) && y != 0) return x / y;
        if (itemStack.is(Items.MIN_CARD.asItem())) return Math.min(x, y);
        if (itemStack.is(Items.MAX_CARD.asItem())) return Math.max(x, y);
        if (itemStack.is(Items.BSR_CARD.asItem())) return x >> y;
        if (itemStack.is(Items.BSL_CARD.asItem())) return x << y;
        return Integer.MIN_VALUE;
    }

    private void applyOutput(String out, int temp) {
        if (out.startsWith("&&")) {
            switch (out) {
                case "&&0" -> l0 = temp;
                case "&&1" -> l1 = temp;
                case "&&2" -> l2 = temp;
                case "&&3" -> l3 = temp;
            }
        } else if (out.startsWith("&")) {
            MEDataControllerBE controller = getGridController();
            if (controller != null)
                controller.addVariable(this.identifier, out.replace("&", ""), temp, 0);
        }
    }

    private MEDataControllerBE getGridController() {
        if (getGridNode() != null && getGridNode().getGrid() != null &&
                !getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()) {
            return getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        }
        return null;
    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int val = rand.nextInt(16); // 0-15
            sb.append(Integer.toHexString(val).toUpperCase());
        }
        return sb.toString();
    }

}
