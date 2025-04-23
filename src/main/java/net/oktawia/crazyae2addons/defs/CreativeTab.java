package net.oktawia.crazyae2addons.defs;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.core.definitions.ItemDefinition;
import appeng.items.AEBaseItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.oktawia.crazyae2addons.CrazyAddons;

import java.util.ArrayList;

public class CreativeTab {
    public static final ResourceLocation ID = CrazyAddons.makeId("tab");

    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .title(Component.literal("CAE2"))
            .icon(Blocks.CRAFTING_CANCELER_BLOCK::stack)
            .displayItems(CreativeTab::populateTab)
            .build();

    private static void populateTab(CreativeModeTab.ItemDisplayParameters ignored, CreativeModeTab.Output output) {
        var itemDefs = new ArrayList<ItemDefinition<?>>();
        itemDefs.addAll(Items.getItems());
        itemDefs.addAll(Blocks.getBlocks());

        for (var itemDef : itemDefs) {
            var item = itemDef.asItem();

            if (item instanceof AEBaseBlockItem baseItem && baseItem.getBlock() instanceof AEBaseBlock baseBlock) {
                baseBlock.addToMainCreativeTab(output);
            } else if (item instanceof AEBaseItem baseItem) {
                baseItem.addToMainCreativeTab(output);
            } else {
                output.accept(itemDef);
            }
        }
    }
}