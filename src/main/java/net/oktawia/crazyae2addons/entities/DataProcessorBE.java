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
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.core.definitions.AEItems;
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
import net.oktawia.crazyae2addons.Parts.DataExtractorPart;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Items;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.DataProcessorMenu;
import net.oktawia.crazyae2addons.menus.MEDataControllerMenu;
import net.oktawia.crazyae2addons.records.LogicSetting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DataProcessorBE extends NotifyableBlockEntity implements MenuProvider, IUpgradeableObject {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 9);
    public DataProcessorMenu menu;
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(Blocks.DATA_PROCESSOR_BLOCK, 0, this::saveChanges);
    public Integer submenuNum;

    public Map<Integer, LogicSetting> settings = Map.of(
            0, new LogicSetting("", "", ""),
            1, new LogicSetting("", "", ""),
            2, new LogicSetting("", "", ""),
            3, new LogicSetting("", "", ""),
            4, new LogicSetting("", "", ""),
            5, new LogicSetting("", "", ""),
            6, new LogicSetting("", "", ""),
            7, new LogicSetting("", "", ""),
            8, new LogicSetting("", "", "")
    );
    public String in;
    public String out;

    public DataProcessorBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(4);
        for(int i = 0; i < inv.size(); i ++){
            this.inv.setMaxStackSize(i, 1);
        }
    }

    @Override
    public void doNotify(Integer value) {
        MEDataControllerBE dataBase = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        compute(dataBase, value);
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

    public static String parseSettings(Map<Integer, LogicSetting> settings){
        return settings.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue().in1 + ":" + e.getValue().in2 + ":" + e.getValue().out)
                .collect(Collectors.joining("|"));
    }

    public static Map<Integer, LogicSetting> loadSettings(String input){
        return Arrays.stream(input.split("\\|"))
                .filter(s -> !s.isEmpty())
                .map(s -> s.split(":", -1))
                .collect(Collectors.toMap(
                        arr -> Integer.parseInt(arr[0]),
                        arr -> new LogicSetting(arr[1], arr[2], arr[3])
                ));
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.settings = loadSettings(data.getString("cardsettings"));
    }

    @Override
    public void saveAdditional(CompoundTag data){
        super.saveAdditional(data);
        data.putString("cardsettings", parseSettings(this.settings));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DataProcessorMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.DATA_PROCESSOR_MENU, player, locator);
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

    public void compute(MEDataControllerBE database, int value){
        int l0;
        int l1;
        int l2;
        int l3;
        String in1;
        String in2;
        String out;
        int temp;
        int i = 0;
        while(i < this.getInternalInventory().size()){
            if(i == 0){
                in1 = this.in;
            } else {
                in1 = this.settings.get(i).in1;
            }
            in2 = this.settings.get(i).in2;
            out = this.settings.get(i).out;
            int x;
            int y;
            int z;

            if (in1.startsWith("&&")) {

            } else if (in1.startsWith("&")) {
                x = database.getVariable(in1);
            } else {
                try {
                    x = Integer.parseInt(in1);
                } catch (NumberFormatException e) {
                    x = null;
                }
            }
            if (in2.startsWith("&")){
                y = database.getVariable(in1);
            }
            var itemStack = getInternalInventory().getStackInSlot(i);
            if(itemStack.is(Items.ADD_CARD.asItem())){
                temp = x + y
            }

            if (out.startsWith("&")){
                z = database.getVariable(in1);
            }
            i ++;
        }
    }

    public void notifyDatabase() {
        if (getMainNode().getGrid() == null){
            return;
        }
        MEDataControllerBE dataBase = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        dataBase.registerNotification(this.in, this);
    }
}
