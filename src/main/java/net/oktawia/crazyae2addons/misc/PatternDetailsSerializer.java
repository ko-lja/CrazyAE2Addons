package net.oktawia.crazyae2addons.misc;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.AEKey;
import net.minecraft.nbt.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class PatternDetailsSerializer {

    public static CompoundTag serialize(IPatternDetails details) {
        CompoundTag root = new CompoundTag();
        root.put("def", details.getDefinition().toTag());

        ListTag inputsList = new ListTag();
        for (IInput input : details.getInputs()) {
            ListTag inputTag = new ListTag();
            for (GenericStack stack : input.getPossibleInputs()) {
                inputTag.add(GenericStack.writeTag(stack));
            }
            inputsList.add(inputTag);
        }
        root.put("input", inputsList);

        ListTag outputsList = new ListTag();
        for (GenericStack stack : details.getOutputs()) {
            outputsList.add(GenericStack.writeTag(stack));
        }
        root.put("output", outputsList);

        root.putBoolean("push", details.supportsPushInputsToExternalInventory());
        return root;
    }
    public static IPatternDetails deserialize(CompoundTag tag) {
        AEItemKey def = AEItemKey.fromTag(tag.getCompound("def"));

        ListTag inputsList = (ListTag) tag.get("input");
        IInput[] inputs = new IInput[inputsList.size()];
        for (int i = 0; i < inputsList.size(); i++) {
            Tag inputEntry = inputsList.get(i);
            if (!(inputEntry instanceof ListTag)) continue;
            ListTag inputTag = (ListTag) inputEntry;
            List<GenericStack> stacks = new ArrayList<>();
            for (int j = 0; j < inputTag.size(); j++) {
                CompoundTag stackTag = inputTag.getCompound(j);
                GenericStack stack = GenericStack.readTag(stackTag);
                stacks.add(stack);
            }
            GenericStack[] possibleInputs = stacks.toArray(new GenericStack[0]);
            inputs[i] = new IInput() {
                @Override
                public GenericStack[] getPossibleInputs() {
                    return possibleInputs;
                }
                @Override
                public long getMultiplier() {
                    return 1;
                }
                @Override
                public boolean isValid(AEKey input, Level level) {
                    return false;
                }
                @Override
                public AEKey getRemainingKey(AEKey template) {
                    return null;
                }
            };
        }

        ListTag outputsList = (ListTag) tag.get("output");
        List<GenericStack> outputStacks = new ArrayList<>();
        for (int i = 0; i < outputsList.size(); i++) {
            GenericStack stack = GenericStack.readTag(outputsList.getCompound(i));
            outputStacks.add(stack);
        }
        GenericStack[] outputs = outputStacks.toArray(new GenericStack[0]);

        boolean push = tag.getBoolean("push");
        return new PatternDetails(def, inputs, outputs, push);
    }

    public static class PatternDetails implements IPatternDetails {
        private final AEItemKey definition;
        private final IInput[] inputs;
        private final GenericStack[] outputs;
        private final boolean supportsPush;

        public PatternDetails(AEItemKey definition, IInput[] inputs, GenericStack[] outputs, boolean supportsPush) {
            this.definition = definition;
            this.inputs = inputs;
            this.outputs = outputs;
            this.supportsPush = supportsPush;
        }

        @Override
        public AEItemKey getDefinition() {
            return definition;
        }

        @Override
        public IInput[] getInputs() {
            return inputs;
        }

        @Override
        public GenericStack[] getOutputs() {
            return outputs;
        }

        @Override
        public boolean supportsPushInputsToExternalInventory() {
            return supportsPush;
        }
    }
}
