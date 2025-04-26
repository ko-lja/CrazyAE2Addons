package net.oktawia.crazyae2addons.parts;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.automation.IOBusPart;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.implementations.StackTransferContextImplementation;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.prioritylist.DefaultPriorityList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.oktawia.crazyae2addons.menus.NBTExportBusMenu;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class NBTExportBusPart extends IOBusPart {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/export_bus_base");

    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/export_bus_off"));

    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/export_bus_on"));

    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/export_bus_has_channel"));

    @Nullable
    private StackExportStrategy exportStrategy;
    public boolean matchmode;
    public String data;
    private NBTExportBusMenu menu;

    public NBTExportBusPart(IPartItem<?> partItem) {
        super(TickRates.ExportBus, StackWorldBehaviors.hasExportStrategyFilter(), partItem);
        matchmode = false;
        data = "";
    }

    public boolean doesItemMatch(AEItemKey item, CompoundTag criteria, boolean matchAll) {
        if (item == null || item.getTag() == null) {
            return false;
        }
        CompoundTag itemTag = item.getTag();

        if (isSingleAny(criteria)) {
            return matchAll ? false : true;
        }

        boolean allowExtraTags = containsAnyTag(criteria);

        return matchAll
                ? matchesAll(itemTag, criteria, allowExtraTags)
                : matchesAny(itemTag, criteria);
    }

    private boolean isSingleAny(CompoundTag criteria) {
        if (criteria.getAllKeys().size() == 1 && criteria.contains("ANY")) {
            Tag tag = criteria.get("ANY");
            return isAny(tag);
        }
        return false;
    }

    private boolean containsAnyTag(CompoundTag criteria) {
        for (String key : criteria.getAllKeys()) {
            Tag tag = criteria.get(key);
            if ("ANY".equals(key) && isAny(tag)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAll(CompoundTag itemTag, CompoundTag criteria, boolean allowExtraTags) {
        if (!allowExtraTags && itemTag.getAllKeys().size() != criteria.getAllKeys().size()) {
            return false;
        }
        for (String critKey : criteria.getAllKeys()) {
            Tag critValue = criteria.get(critKey);
            if (critKey.equals("ANY")) {
                if (!existsAnyMatch(itemTag, critValue)) {
                    return false;
                }
            } else if (isAny(critValue)) {
                if (!itemTag.contains(critKey)) {
                    return false;
                }
            } else {
                if (!itemTag.contains(critKey) || !itemTag.get(critKey).equals(critValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesAny(CompoundTag itemTag, CompoundTag criteria) {
        for (String critKey : criteria.getAllKeys()) {
            Tag critValue = criteria.get(critKey);
            if (critKey.equals("ANY") && existsAnyMatch(itemTag, critValue)) {
                return true;
            } else if (isAny(critValue) && itemTag.contains(critKey)) {
                return true;
            } else if (itemTag.contains(critKey) && itemTag.get(critKey).equals(critValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean existsAnyMatch(CompoundTag itemTag, Tag critValue) {
        for (String itemKey : itemTag.getAllKeys()) {
            if (itemTag.get(itemKey).equals(critValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAny(Tag tag) {
        return tag instanceof StringTag && ((StringTag) tag).getAsString().equals("ANY");
    }



    public NonNullList<AEItemKey> getItemKeys(KeyCounter keyCounter){
        NonNullList<AEItemKey> keys = NonNullList.create();
        keyCounter.forEach(
                key -> {
                    if (key.getKey() instanceof AEItemKey){
                        keys.add((AEItemKey) key.getKey());
                    }
                }
        );
        return keys;
    }

    public static CompoundTag strToNBT(String input) {
        try {
            return TagParser.parseTag(input);
        } catch (CommandSyntaxException e) {
            return new CompoundTag();
        }
    }

    protected final StackExportStrategy getExportStrategy() {
        if (exportStrategy == null) {
            var self = this.getHost().getBlockEntity();
            var fromPos = self.getBlockPos().relative(this.getSide());
            var fromSide = getSide().getOpposite();
            exportStrategy = StackWorldBehaviors.createExportFacade((ServerLevel) getLevel(), fromPos, fromSide);
        }
        return exportStrategy;
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        var storageService = grid.getStorageService();
        var context = createTransferContext(storageService, grid.getEnergyService());
        var stacks = grid.getStorageService().getInventory().getAvailableStacks();
        if (stacks.isEmpty()) {
            return false;
        }

        var availableKeys = getItemKeys(stacks);
        NonNullList<AEItemKey> matchingKeys = availableKeys.stream()
                .filter(item -> doesItemMatch(item, strToNBT(this.data), !this.matchmode))
                .collect(Collectors.toCollection(NonNullList::create));

        if (matchingKeys.isEmpty()) {
            return false;
        }

        boolean didWork = false;
        for (AEItemKey key : matchingKeys) {
            int transferFactor = 4;
            long amount = (long) context.getOperationsRemaining() * transferFactor;
            long transferred = getExportStrategy().transfer(context, key, amount);
            if (transferred > 0) {
                context.reduceOperationsRemaining(Math.max(1, transferred / transferFactor));
                didWork = true;
            }
            if (!context.hasOperationsLeft()) {
                break;
            }
        }

        return didWork;
    }


    private StackTransferContextImplementation createTransferContext(IStorageService storageService, IEnergyService energyService) {
        return new StackTransferContextImplementation(
                storageService,
                energyService,
                this.source,
                getOperationsPerTick(),
                DefaultPriorityList.INSTANCE) {
        };
    }

    @Override
    protected MenuType<?> getMenuType() {
        return CrazyMenuRegistrar.NBT_EXPORT_BUS_MENU.get();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    public void setMenu(NBTExportBusMenu menu){
        this.menu = menu;
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        if(extra.contains("filter")){
            this.data = extra.getString("filter");
        }
        if(extra.contains("matchmode")){
            this.matchmode = extra.getBoolean("matchmode");
        }
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        extra.putString("filter", this.data);
        extra.putBoolean("matchmode", this.matchmode);
    }
}
