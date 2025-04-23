package net.oktawia.crazyae2addons.datagen;

import appeng.core.definitions.ItemDefinition;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.Items;

public class CrazyItemModelProvider extends ItemModelProvider {
    public CrazyItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CrazyAddons.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var item : Items.getItems()){
            if (!Items.getParts().contains(item)){
                simpleItem(item);
            }
        }
    }

    private ItemModelBuilder simpleItem(ItemDefinition<?> item){
        return withExistingParent(item.id().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(CrazyAddons.MODID, "item/" + item.id().getPath()));
    }
}
