package net.oktawia.crazyae2addons.xei.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.oktawia.crazyae2addons.xei.common.CrazyEntry;

import java.util.List;

public class CrazyEmiRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final CrazyEntry entry;
    private final List<EmiStack> inputs;

    public CrazyEmiRecipe(CrazyEntry entry) {
        this.entry = entry;
        this.id = entry.structureId();
        this.inputs = entry.requiredItems().stream().map(EmiStack::of).toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CrazyEmiPlugin.CRAZY_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiStack> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(); // Brak outputu
    }

    @Override
    public int getDisplayWidth() {
        return 160; // jak w CrazyPreview
    }

    @Override
    public int getDisplayHeight() {
        return 200; // jak w CrazyPreview
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new CrazyPreview(entry.structureId(), entry.requiredItems(), entry.name().getString()));
    }
}