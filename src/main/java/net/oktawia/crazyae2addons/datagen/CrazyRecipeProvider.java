package net.oktawia.crazyae2addons.datagen;

import appeng.core.definitions.AEBlocks;
import com.mojang.logging.LogUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.oktawia.crazyae2addons.defs.BlockDefs;
import net.oktawia.crazyae2addons.defs.ItemDefs;

import java.util.Map;
import java.util.function.Consumer;

public class CrazyRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public CrazyRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        for (var entry : BlockDefs.getBlockRecipes().entrySet()){
            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, entry.getKey());
            for (var recipe : entry.getValue().getKey().split("/")) {
                LogUtils.getLogger().info(recipe);
                builder.pattern(recipe);
            }
            for (Map.Entry<String, Item> e : entry.getValue().getValue().entrySet()) {
                builder.define(e.getKey().charAt(0), e.getValue());
            }
            builder.unlockedBy(getHasName(AEBlocks.CONTROLLER.asItem()), has(AEBlocks.CONTROLLER.asItem()));
            builder.save(pWriter);
        }
        for (var entry : ItemDefs.getItemRecipes().entrySet()){
            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, entry.getKey());
            for (var recipe : entry.getValue().getKey().split("/")) {
                LogUtils.getLogger().info(recipe);
                builder.pattern(recipe);
            }
            for (Map.Entry<String, Item> e : entry.getValue().getValue().entrySet()) {
                builder.define(e.getKey().charAt(0), e.getValue());
            }
            builder.unlockedBy(getHasName(AEBlocks.CONTROLLER.asItem()), has(AEBlocks.CONTROLLER.asItem()));
            builder.save(pWriter);
        }
    }
}
