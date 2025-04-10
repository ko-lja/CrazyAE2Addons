package net.oktawia.crazyae2addons.entities;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
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
import net.oktawia.crazyae2addons.misc.NBTContainer;
import net.oktawia.crazyae2addons.misc.LogicSetting;
import org.jetbrains.annotations.Nullable;
import org.jline.utils.Log;

import java.security.SecureRandom;
import java.util.HashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DataProcessorBE extends NotifyableBlockEntity implements MenuProvider, IUpgradeableObject {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 9);
    public DataProcessorMenu menu;
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(Blocks.DATA_PROCESSOR_BLOCK, 0, this::saveChanges);
    public Integer submenuNum;
    public String identifier;
    public NBTContainer settings = new NBTContainer();
    public String in = "";

    public DataProcessorBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(4);
        for(int i = 0; i < inv.size(); i ++){
            this.inv.setMaxStackSize(i, 1);
            settings.set(String.valueOf(i), new LogicSetting());
        }
    }

    @Override
    public void doNotify(String name, Integer value) {
        if (getMainNode().getGrid() == null){
            return;
        }
        MEDataControllerBE database = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        compute(database);
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

    public void compute(MEDataControllerBE database){
        int l0 = 0;
        int l1 = 0;
        int l2 = 0;
        int l3 = 0;
        String in1;
        String in2;
        String out;
        int temp = 0;
        int i = 0;
        while(i < this.getInternalInventory().size()){
            if(i == 0){
                in1 = this.in;
            } else {
                in1 = ((LogicSetting) this.settings.get(String.valueOf(i))).in1;
            }
            in2 = ((LogicSetting) this.settings.get(String.valueOf(i))).in2;
            out = ((LogicSetting) this.settings.get(String.valueOf(i))).out;
            int x = 0;
            int y = 0;

            if (in1.startsWith("&&")) {
                switch (in1){
                    case "&&0" -> x = l0;
                    case "&&1" -> x = l1;
                    case "&&2" -> x = l2;
                    case "&&3" -> x = l3;
                }
            } else if (in1.startsWith("&")) {
                x = database.getVariable(in1.replace("&", ""));
            } else {
                try {
                    x = Integer.parseInt(in1);
                } catch (Exception ignored){}
            }

            if (in2.startsWith("&&")) {
                switch (in2){
                    case "&&0" -> y = l0;
                    case "&&1" -> y = l1;
                    case "&&2" -> y = l2;
                    case "&&3" -> y = l3;
                }
            } else if (in2.startsWith("&")) {
                y = database.getVariable(in2.replace("&", ""));
            } else {
                try {
                    y = Integer.parseInt(in2);
                } catch (Exception ignored){}
            }
            var itemStack = getInternalInventory().getStackInSlot(i);
            if(itemStack.is(Items.ADD_CARD.asItem())){
                temp = x + y;
            }
            if(itemStack.is(Items.SUB_CARD.asItem())){
                temp = x - y;
            }
            if(itemStack.is(Items.MUL_CARD.asItem())){
                temp = x * y;
            }
            if(itemStack.is(Items.DIV_CARD.asItem()) && y != 0){
                temp = x / y;
            }
            if(itemStack.is(Items.MIN_CARD.asItem())){
                temp = min(x, y);
            }
            if(itemStack.is(Items.MAX_CARD.asItem())){
                temp = max(x, y);
            }
            if(itemStack.is(Items.BSR_CARD.asItem())){
                temp = x >> y;
            }
            if(itemStack.is(Items.BSL_CARD.asItem())){
                temp = x << y;
            }
            if(itemStack.is(Items.HIT_CARD.asItem())){
                if (x > 0){
                    i = y;
                    continue;
                }
            }
            if(itemStack.is(Items.HIF_CARD.asItem())){
                if (x <= 0){
                    i = y;
                    continue;
                }
            }
            if (out.startsWith("&&")) {
                switch (out){
                    case "&&0" -> l0 = temp;
                    case "&&1" -> l1 = temp;
                    case "&&2" -> l2 = temp;
                    case "&&3" -> l3 = temp;
                }
            } else if (out.startsWith("&")) {
                database.addVariable(this.identifier, out.replace("&", ""), temp);
            }
            i ++;
        }
    }

    public void notifyDatabase() {
        if (getMainNode().getGrid() == null){
            return;
        }
        MEDataControllerBE dataBase = getMainNode().getGrid().getMachines(MEDataControllerBE.class).stream().toList().get(0);
        dataBase.registerNotification(this.in.replace("&", ""), this);
    }
}
