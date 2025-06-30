package net.oktawia.crazyae2addons.jei;

import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import org.jetbrains.annotations.NotNull;

public class CrazyCategory extends ModularUIRecipeCategory<CrazyWrapper> {

    public static final ResourceLocation UID = new ResourceLocation("crazyae2addons", "crazy_multiblocks");
    public static final RecipeType<CrazyWrapper> TYPE = new RecipeType<>(UID, CrazyWrapper.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CrazyCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 180);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(CrazyItemRegistrar.SUPER_SINGULARITY.get()));
    }

    @Override
    public RecipeType<CrazyWrapper> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crazy Multiblocks");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
