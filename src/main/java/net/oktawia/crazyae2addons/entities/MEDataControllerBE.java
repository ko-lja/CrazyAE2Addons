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
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.core.definitions.AEItems;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.misc.DataVariable;
import net.oktawia.crazyae2addons.misc.NBTContainer;
import net.oktawia.crazyae2addons.misc.NotificationData;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;
import net.oktawia.crazyae2addons.parts.NotifyablePart;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.MEDataControllerMenu;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class MEDataControllerBE extends AENetworkInvBlockEntity implements IGridTickable, MenuProvider, IUpgradeableObject {

    public AppEngCellInventory inv = new AppEngCellInventory(this, 6);
    public MEDataControllerMenu menu;
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(CrazyBlockRegistrar.ME_DATA_CONTROLLER_BLOCK.get(), 0, this::saveChanges);
    public NBTContainer variables = new NBTContainer();
    public NBTContainer toNotify = new NBTContainer();

    public MEDataControllerBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.ME_DATA_CONTROLLER_BE.get(), pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(4).addService(IGridTickable.class, this)
                .setVisualRepresentation(
                        new ItemStack(CrazyBlockRegistrar.ME_DATA_CONTROLLER_BLOCK.get().asItem())
                );
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.variables.clear();
        if (this.getMenu() != null){
            menu.maxVariables = menu.getMaxVariables();
            menu.variableNum = menu.getVariableNum();
        }
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
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putByteArray("variables", variables.serialize(true));
        data.putByteArray("tonotify", toNotify.serialize(true));
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        try{
            if (data.contains("variables")) {
                variables.deserialize(data.getByteArray("variables"));
            }
        } catch (Exception ignored) {}
        try {
            if (data.contains("tonotify")) {
                toNotify.deserialize(data.getByteArray("tonotify"));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MEDataControllerMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.ME_DATA_CONTROLLER_MENU.get(), player, locator);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    public int getMaxVariables() {
        int maxVariables = 0;
        InternalInventory cellInv = getInternalInventory();
        for (ItemStack stack : cellInv){
            if (stack.getItem() == AEItems.CELL_COMPONENT_1K.asItem()){
                maxVariables = maxVariables + 1;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_4K.asItem()) {
                maxVariables = maxVariables + 4;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_16K.asItem()) {
                maxVariables = maxVariables + 16;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_64K.asItem()) {
                maxVariables = maxVariables + 64;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_256K.asItem()) {
                maxVariables = maxVariables + 256;
            }
        }
        return maxVariables;
    }

    public void setMenu(MEDataControllerMenu menu){
        this.menu = menu;
    }

    public MEDataControllerMenu getMenu(){
        return this.menu;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, true);
    }

    public void addVariable(String id, String name, Integer value, Integer depth){
        this.saveChanges();
        if (this.variables.get(id) != null || this.variables.size() < this.getMaxVariables()) {
            if (this.variables.get(id) != null && !Objects.equals(((DataVariable) this.variables.get(id)).name, name)){
                addVariable(id, ((DataVariable) this.variables.get(id)).name, 0, depth);
            }
            this.variables.set(id, new DataVariable(name, value));
            if (toNotify.get(name) != null){
                var list = ((NotificationData) toNotify.get(name)).requesters;
                var iterator = list.iterator();
                while (iterator.hasNext()) {
                    var def = iterator.next();
                    var requester = NotificationData.get(def, this.getLevel().getServer());
                    if (requester instanceof BlockEntity) {
                        ((NotifyableBlockEntity) requester).doNotify(name, value, depth);
                    } else if (requester instanceof AEBasePart) {
                        ((NotifyablePart) requester).doNotify(name, value, depth);
                    } else if (requester == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public Integer getVariable(String key) {
        return this.variables.toStream()
                .filter(nd -> Objects.equals(((Map.Entry<String, DataVariable>) nd).getValue().name, key))
                .map(nd -> ((Map.Entry<String, DataVariable>) nd).getValue().value)
                .findFirst()
                .orElse(0);
    }

    public void registerNotification(String variable, AEBaseBlockEntity target){
        if (this.toNotify.get(variable) == null){
            this.toNotify.set(variable, new NotificationData());
        }
        ((NotificationData) this.toNotify.get(variable)).addRequester(target);
    }

    public void registerNotification(String variable, AEBasePart target){
        if (this.toNotify.get(variable) == null){
            this.toNotify.set(variable, new NotificationData());
        }
        ((NotificationData) this.toNotify.get(variable)).addRequester(target);
    }

    public void unRegisterNotification(AEBaseBlockEntity target){
        this.toNotify.toStream().forEach(entry -> {
            ((Map.Entry<String, NotificationData>) entry).getValue().removeRequester(target);
        });
    }

    public void unRegisterNotification(AEBasePart target){
        this.toNotify.toStream().forEach(entry -> {
            ((Map.Entry<String, NotificationData>) entry).getValue().removeRequester(target);
        });
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        Set<MEDataControllerBE> controllers = getMainNode().getGrid().getMachines(MEDataControllerBE.class);
        for (MEDataControllerBE controller : controllers){
            if (!controller.getBlockPos().equals(this.getBlockPos())){
                getLevel().destroyBlock(getBlockPos(), true);
            }
        }
        if (getMainNode().getGrid() != null) {
            Set<String> existingMachines = new HashSet<>();
            for (DataExtractorPart extractor : getMainNode().getGrid().getMachines(DataExtractorPart.class)) {
                existingMachines.add(extractor.identifier);
            }

            List<DataVariable> variablesCopy = this.variables.toStream()
                    .map(nd -> ((Map.Entry<String, DataVariable>) nd).getValue())
                    .toList();
            for (DataVariable dv : variablesCopy) {
                if (!existingMachines.contains(dv.name)) {
                    this.variables.del(dv.name);
                }
            }
        }
        return TickRateModulation.IDLE;
    }
}
