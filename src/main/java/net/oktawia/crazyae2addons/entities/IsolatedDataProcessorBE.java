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
        if (i != 0) {
            in1 = ((LogicSetting) this.settings.get(String.valueOf(i))).in1;
        } else {
            in1 = "0";
        }
        in2 = ((LogicSetting) this.settings.get(String.valueOf(i))).in2;
        out = ((LogicSetting) this.settings.get(String.valueOf(i))).out;

        if (in1.startsWith("&&")) {
            switch (in1) {
                case "&&0" -> x = l0;
                case "&&1" -> x = l1;
                case "&&2" -> x = l2;
                case "&&3" -> x = l3;
            }
        } else if (in1.startsWith("&")) {
            if (this.getGridNode() != null && this.getGridNode().getGrid() != null && !this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()) {
                MEDataControllerBE controller = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
                x = controller.getVariable(in1.replace("&", ""));
            } else {
                x = 0;
            }
        } else {
            try {
                x = Integer.parseInt(in1);
            } catch (Exception ignored) {
            }
        }

        if (in2.startsWith("&&")) {
            switch (in2) {
                case "&&0" -> y = l0;
                case "&&1" -> y = l1;
                case "&&2" -> y = l2;
                case "&&3" -> y = l3;
            }
        } else if (in2.startsWith("&")) {
            if (this.getGridNode() != null && this.getGridNode().getGrid() != null && !this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()) {
                MEDataControllerBE controller = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
                y = controller.getVariable(in2.replace("&", ""));
            } else {
                y = 0;
            }
        } else {
            try {
                y = Integer.parseInt(in2);
            } catch (Exception ignored) {
            }
        }
        int temp = 0;
        if (itemStack.is(Items.ADD_CARD.asItem())) {
            temp = x + y;
        } else if (itemStack.is(Items.SUB_CARD.asItem())) {
            temp = x - y;
        } else if (itemStack.is(Items.MUL_CARD.asItem())) {
            temp = x * y;
        } else if (itemStack.is(Items.DIV_CARD.asItem()) && y != 0) {
            temp = x / y;
        } else if (itemStack.is(Items.MIN_CARD.asItem())) {
            temp = min(x, y);
        } else if (itemStack.is(Items.MAX_CARD.asItem())) {
            temp = max(x, y);
        } else if (itemStack.is(Items.BSR_CARD.asItem())) {
            temp = x >> y;
        } else if (itemStack.is(Items.BSL_CARD.asItem())) {
            temp = x << y;
        } else if (itemStack.is(Items.HIT_CARD.asItem())) {
            if (x > 0) {
                i = y;
                return TickRateModulation.IDLE;
            } else {
                i++;
                return TickRateModulation.IDLE;
            }
        } else if (itemStack.is(Items.HIF_CARD.asItem())) {
            if (x <= 0) {
                i = y;
                return TickRateModulation.IDLE;
            } else {
                i++;
                return TickRateModulation.IDLE;
            }
        }
        if (out.startsWith("&&")) {
            switch (out) {
                case "&&0" -> l0 = temp;
                case "&&1" -> l1 = temp;
                case "&&2" -> l2 = temp;
                case "&&3" -> l3 = temp;
            }
        } else if (out.startsWith("&")) {
            if (this.getGridNode() != null && this.getGridNode().getGrid() != null && !this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()) {
                MEDataControllerBE controller = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
                controller.addVariable(this.identifier, out.replace("&", ""), temp, 0);
            }
        }
        i++;
        return TickRateModulation.IDLE;
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
