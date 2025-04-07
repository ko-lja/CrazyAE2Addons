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
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.core.definitions.AEItems;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.Parts.DataExtractorPart;
import net.oktawia.crazyae2addons.blocks.MEDataControllerBlock;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.MEDataControllerMenu;
import net.oktawia.crazyae2addons.misc.BlockEntityDescription;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class MEDataControllerBE extends NotifyableBlockEntity implements IGridTickable, MenuProvider, IUpgradeableObject {

    public AppEngCellInventory inv = new AppEngCellInventory(this, 6);
    public MEDataControllerMenu menu;
    public HashMap<String, Map<String, Integer>> variables = new HashMap<>();
    public IUpgradeInventory upgrades = UpgradeInventories.forMachine(Blocks.ME_DATA_CONTROLLER_BLOCK, 0, this::saveChanges);
    public Map<String, List<BlockEntityDescription>> toNotify = Collections.emptyMap();

    private ListTag serializeVariables() {
        ListTag list = new ListTag();
        for (var outerEntry : variables.entrySet()) {
            String outerKey = outerEntry.getKey();
            for (var innerEntry : outerEntry.getValue().entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("outerKey", outerKey);
                tag.putString("innerKey", innerEntry.getKey());
                tag.putInt("value", innerEntry.getValue());
                list.add(tag);
            }
        }
        return list;
    }

    private void deserializeVariables(ListTag list) {
        variables.clear();
        for (Tag t : list) {
            CompoundTag tag = (CompoundTag) t;
            String outer = tag.getString("outerKey");
            String inner = tag.getString("innerKey");
            int value = tag.getInt("value");
            variables.computeIfAbsent(outer, k -> new HashMap<>()).put(inner, value);
        }
    }

    private ListTag serializeToNotify() {
        ListTag list = new ListTag();
        for (var entry : toNotify.entrySet()) {
            String var = entry.getKey();
            for (BlockEntityDescription desc : entry.getValue()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("variable", var);
                tag.putString("desc", desc.serialize());
                list.add(tag);
            }
        }
        return list;
    }

    private void deserializeToNotify(ListTag list, Function<String, Level> levelResolver) {
        Map<String, List<BlockEntityDescription>> map = new HashMap<>();
        for (Tag t : list) {
            CompoundTag tag = (CompoundTag) t;
            String var = tag.getString("variable");
            String descStr = tag.getString("desc");
            BlockEntityDescription desc = BlockEntityDescription.deserialize(descStr, levelResolver);
            map.computeIfAbsent(var, k -> new ArrayList<>()).add(desc);
        }
        toNotify = map;
    }

    public MEDataControllerBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(4).addService(IGridTickable.class, this);
    }

    @Override
    public void doNotify(Integer value) {

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
        data.put("variables", serializeVariables());
        data.put("toNotify", serializeToNotify());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("variables")) {
            deserializeVariables(data.getList("variables", Tag.TAG_COMPOUND));
        }
        if (data.contains("toNotify")) {
            deserializeToNotify(data.getList("toNotify", Tag.TAG_COMPOUND), this::resolveLevelFromId);
        }
    }

    private Level resolveLevelFromId(String id) {
        if (this.level == null || this.level.getServer() == null) return null;
        ResourceLocation dimensionId = ResourceLocation.tryParse(id);
        if (dimensionId == null) return null;

        return this.level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MEDataControllerMenu(i, inventory, this);
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Menus.ME_DATA_CONTROLLER_MENU, player, locator);
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

    public void addVariable(String id, String name, Integer value){
        if (this.variables.containsKey(id) || (this.variables.size() < this.getMaxVariables() && this.variables.values().stream().noneMatch(map -> map.containsKey(name)))) {
            this.variables.put(id, Map.of(name, value));
            if (toNotify.containsKey(name)){
                toNotify.get(name).forEach(def -> {
                    ((NotifyableBlockEntity) def.get()).doNotify(value);
                });
            }
        }
    }

    public Integer getVariable(String key) {
        for (Map<String, Integer> nestedMap : variables.values()) {
            if (nestedMap.containsKey(key)) {
                return nestedMap.get(key);
            }
        }
        return null;
    }

    public void registerNotification(String variable, AEBaseBlockEntity target){
        this.toNotify.computeIfAbsent(variable, k -> new ArrayList<>()).add(new BlockEntityDescription(target));
    }

    public void registerNotification(String variable, AEBasePart target){
        this.toNotify.computeIfAbsent(variable, k -> new ArrayList<>()).add(new BlockEntityDescription(target));
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        Set<MEDataControllerBE> controllers = getMainNode().getGrid().getMachines(MEDataControllerBE.class);
        for (MEDataControllerBE controller : controllers){
            if (!controller.getBlockPos().equals(this.getBlockPos())){
                getLevel().destroyBlock(getBlockPos(), true);
            }
        }
        if (getMainNode().getGrid() != null){
            Set<String> existingMachines = new HashSet<>();
            for (DataExtractorPart extractor : getMainNode().getGrid().getMachines(DataExtractorPart.class)) {
                existingMachines.add(extractor.identifier);
            }

            Iterator<Map.Entry<String, Map<String, Integer>>> iterator = this.variables.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Map<String, Integer>> entry = iterator.next();
                if (!existingMachines.contains(entry.getKey())) {
                    iterator.remove();
                }
            }
        }

        return TickRateModulation.IDLE;
    }
}
