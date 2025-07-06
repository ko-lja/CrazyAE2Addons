package net.oktawia.crazyae2addons.xei.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.xei.common.CrazyEntry;

import java.util.List;

public class CrazyEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory CRAZY_CATEGORY = new EmiRecipeCategory(
            new ResourceLocation("crazyae2addons", "crazy_multiblocks"),
            EmiStack.of(new ItemStack(CrazyItemRegistrar.SUPER_SINGULARITY.get()))
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CRAZY_CATEGORY);

        List<CrazyEntry> entries = List.of(
                new CrazyEntry(new ResourceLocation("crazyae2addons", "penrose_sphere.nbt"), List.of(
                        new ItemStack(CrazyBlockRegistrar.ENTROPY_CRADLE.get(), 1)
                ), Component.literal("Penrose Sphere"))
        );

        for (CrazyEntry entry : entries) {
            registry.addRecipe(new CrazyEmiRecipe(entry));
        }
    }
}