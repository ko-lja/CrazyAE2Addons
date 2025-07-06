package net.oktawia.crazyae2addons.xei.jei;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.xei.common.CradleEntry;
import net.oktawia.crazyae2addons.xei.common.CrazyEntry;
import net.oktawia.crazyae2addons.xei.common.CrazyRecipes;

import java.util.List;

@JeiPlugin
public class CrazyPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("crazyae2addons", "jei_plugin");
    public static CrazyEntry currentEntry;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CrazyCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ReinforcedCondenserCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CradleCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        List<CrazyWrapper> wrapped = CrazyRecipes.getCrazyEntries().stream().map(CrazyWrapper::new).toList();
        registration.addRecipes(CrazyCategory.TYPE, wrapped);

        registration.addRecipes(ReinforcedCondenserCategory.TYPE, CrazyRecipes.getCondenserEntried());

        List<CradleWrapper> wrapped2 = CrazyRecipes.getCradleEntries().stream()
                .map(CradleWrapper::new)
                .toList();

        registration.addRecipes(CradleCategory.TYPE, wrapped2);
    }
}
