package net.oktawia.crazyae2addons.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.Blocks;
import net.oktawia.crazyae2addons.defs.Items;

public class CrazyLangProvider extends LanguageProvider {
    public CrazyLangProvider(PackOutput output, String locale) {
        super(output, CrazyAddons.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        for (var item : Items.getItems()){
            this.add("item." + item.id().toLanguageKey(), item.getEnglishName());
        }
        for (var block : Blocks.getBlocks()){
            this.add("block." + block.id().toLanguageKey(), block.getEnglishName());
        }
    }
}
