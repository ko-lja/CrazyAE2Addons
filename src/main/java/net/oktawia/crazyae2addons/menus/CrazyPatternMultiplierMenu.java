package net.oktawia.crazyae2addons.menus;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.logic.CrazyPatternMultiplierHost;
import net.oktawia.crazyae2addons.misc.AppEngFilteredSlot;

public class CrazyPatternMultiplierMenu extends AEBaseMenu {

    public static String ACTION_MODIFY_PATTERNS = "actionModifyPatterns";
    public CrazyPatternMultiplierHost host;

    @GuiSync(73)
    public double mult;

    public CrazyPatternMultiplierMenu(int id, Inventory ip, CrazyPatternMultiplierHost host) {
        super(CrazyMenuRegistrar.CRAZY_PATTERN_MULTIPLIER_MENU.get(), id, ip, host);
        this.createPlayerInventorySlots(ip);
        this.host = host;
        this.mult = host.getItemStack().getTag() == null ? 0 : host.getItemStack().getTag().getDouble("mult");
        host.setMenu(this);
        for (int i = 0; i < 36; i++){
            this.addSlot(new AppEngFilteredSlot(host.inv, i, AEItems.PROCESSING_PATTERN.asItem()), SlotSemantics.ENCODED_PATTERN);
        }
        registerClientAction(ACTION_MODIFY_PATTERNS, Double.class, this::modifyPatterns);
    }

    public void modifyPatterns(Double multiplier) {
        if (multiplier <= 0) return;
        CompoundTag tag = this.host.getItemStack().getOrCreateTag();
        tag.putDouble("mult", multiplier);
        this.host.getItemStack().setTag(tag);
        if (isClientSide()) {
            sendClientAction(ACTION_MODIFY_PATTERNS, multiplier);
            return;
        }
        for (int i = 0; i < host.inv.size(); i++) {
            ItemStack is = modify(host.inv.getStackInSlot(i), multiplier, this.getPlayer().level());
            this.host.inv.setItemDirect(i, is);
        }
    }

    public static ItemStack modify(ItemStack stack, double multiplier, Level level) {
        if (!(stack.getItem() instanceof EncodedPatternItem pattern))
            return stack;

        var originalTag = stack.getTag();
        var ignoreNbtTag = originalTag != null && originalTag.contains("ignorenbt") ? originalTag.get("ignorenbt").copy() : null;
        var circuitTag = originalTag != null && originalTag.contains("circuit") ? originalTag.get("circuit").copy() : null;

        var detail = pattern.decode(stack, level, false);
        if (!(detail instanceof AEProcessingPattern process))
            return stack;

        GenericStack[] input  = process.getSparseInputs();
        GenericStack[] output = process.getOutputs();

        if (multiplier < 1) {
            for (GenericStack gs : input) {
                if (gs != null && !(gs.what() instanceof AEFluidKey) && gs.amount() == 1)
                    return stack;
            }
            for (GenericStack gs : output) {
                if (gs != null && !(gs.what() instanceof AEFluidKey) && gs.amount() == 1)
                    return stack;
            }
        }

        GenericStack[] newInputs  = new GenericStack[input.length];
        GenericStack[] newOutputs = new GenericStack[output.length];

        for (int i = 0; i < input.length; i++) {
            if (input[i] != null) {
                int amt = (int) Math.max(Math.floor(input[i].amount() * multiplier), 1);
                newInputs[i] = new GenericStack(input[i].what(), amt);
            }
        }

        for (int i = 0; i < output.length; i++) {
            if (output[i] != null) {
                int amt = (int) Math.max(Math.floor(output[i].amount() * multiplier), 1);
                newOutputs[i] = new GenericStack(output[i].what(), amt);
            }
        }

        ItemStack modifiedStack = PatternDetailsHelper.encodeProcessingPattern(newInputs, newOutputs);

        if (ignoreNbtTag != null) {
            modifiedStack.getTag().put("ignorenbt", ignoreNbtTag);
        }
        if (circuitTag != null) {
            modifiedStack.getTag().put("circuit", circuitTag);
        }

        return modifiedStack;
    }
}
