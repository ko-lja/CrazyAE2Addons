package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.Parts.DataExtractorPart;

public class DataExtractorPartItem extends PartItem<DataExtractorPart> {
    public DataExtractorPartItem(Properties properties) {
        super(properties, DataExtractorPart.class, DataExtractorPart::new);
    }
}