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
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.util.inv.AppEngInternalInventory;
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
import net.oktawia.crazyae2addons.misc.NBTContainer;
import net.oktawia.crazyae2addons.misc.LogicSetting;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DataProcessorBE extends NotifyableBlockEntity implements MenuProvider, IUpgradeableObject, IGridTickable {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 9);
    public DataProcessorMenu menu;
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(Blocks.DATA_PROCESSOR_BLOCK, 0, this::saveChanges);
    public Integer submenuNum;
    public String identifier;
    public NBTContainer settings = new NBTContainer();
    public String in = "";
    public boolean looped = false;
    public boolean reRegister = false;

    public DataProcessorBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(4)
                .addService(IGridTickable.class, this);
        for(int i = 0; i < inv.size(); i ++){
            this.inv.setMaxStackSize(i, 1);
            settings.set(String.valueOf(i), new LogicSetting());
        }
    }

    @Override
    public void doNotify(String name, Integer value, Integer depth) {
        if (depth > 128 || getMainNode().getGrid() == null){
            this.looped = true;
            return;
        }
        MEDataControllerBE database = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        compute(database, depth);
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
        this.settings.deserialize(data.getByteArray("settings"));
        if(data.contains("identifier")){
            this.identifier = data.getString("identifier");
        }
        if(data.contains("valin")){
            this.in = data.getString("valin");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data){
        super.saveAdditional(data);
        data.putByteArray("settings", this.settings.serialize(true));
        if(this.identifier == null){
            this.identifier = randomHexId();
        }
        data.putString("identifier", this.identifier);
        if (!this.in.isEmpty()){
            data.putString("valin", this.in);
        } else {
            data.remove("valin");
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DataProcessorMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.DATA_PROCESSOR_MENU, player, locator);
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

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public void setMenu(DataProcessorMenu menu){
        this.menu = menu;
    }

    public DataProcessorMenu getMenu(){
        return this.menu;
    }
    public void compute(MEDataControllerBE database, Integer depth) {
        int[] registers = new int[4];
        int i = 0, count = 0, temp = 0;
        while (i < this.getInternalInventory().size()) {
            if (++count > 128) break;
            String in1, in2 = "", out = "";
            if (i == 0) {
                in1 = this.in;
            } else {
                LogicSetting setting = (LogicSetting) this.settings.get(String.valueOf(i));
                in1 = setting.in1;
                in2 = setting.in2;
                out = setting.out;
            }
            if (i == 0) {
                LogicSetting setting = (LogicSetting) this.settings.get(String.valueOf(i));
                in2 = setting.in2;
                out = setting.out;
            }
            int x = resolveOperand(in1, registers, database);
            int y = resolveOperand(in2, registers, database);

            var itemStack = getInternalInventory().getStackInSlot(i);
            if (itemStack.isEmpty()) {
                i++;
                continue;
            }
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
                    continue;
                }
            } else if (itemStack.is(Items.HIF_CARD.asItem())) {
                if (x <= 0) {
                    i = y;
                    continue;
                }
            }

            if (!assignOutput(out, temp, registers, database, depth)) {
                break;
            }
            i++;
        }
    }

    private int resolveOperand(String input, int[] registers, MEDataControllerBE database) {
        if (input.startsWith("&&")) {
            int index = parseRegisterIndex(input);
            return registers[index];
        } else if (input.startsWith("&")) {
            return database.getVariable(input.replace("&", ""));
        }
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean assignOutput(String output, int value, int[] registers, MEDataControllerBE database, int depth) {
        if (output.startsWith("&&")) {
            int index = parseRegisterIndex(output);
            registers[index] = value;
            return true;
        } else if (output.startsWith("&")) {
            database.addVariable(this.identifier, output.replace("&", ""), value, depth + 1);
            return false;
        }
        return true;
    }

    private int parseRegisterIndex(String reg) {
        try {
            return Integer.parseInt(reg.replace("&", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public void notifyDatabase() {
        if (this.getGridNode() == null ||
                this.getGridNode().getGrid() == null ||
                this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()) {
            this.reRegister = true;
            return;
        }
        MEDataControllerBE controller = getMainNode().getGrid()
                .getMachines(MEDataControllerBE.class).stream().findFirst().orElse(null);
        if (controller == null || controller.getMaxVariables() <= 0) {
            this.reRegister = true;
            return;
        }
        controller.unRegisterNotification(this);
        if (!this.in.isEmpty()) {
            controller.registerNotification(this.in.replace("&", ""), this);
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.getGridNode() == null || this.getGridNode().getGrid() == null || this.getGridNode().getGrid().getMachines(MEDataControllerBE.class).isEmpty()){
            this.reRegister = true;
        } else {
            MEDataControllerBE controller = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
            if (controller.getMaxVariables() <= 0){
                this.reRegister = true;
            } else {
                if (this.reRegister){
                    this.reRegister = false;
                    notifyDatabase();
                }
            }
        }
        return TickRateModulation.IDLE;
    }
}
