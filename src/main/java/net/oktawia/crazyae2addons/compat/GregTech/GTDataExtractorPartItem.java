package net.oktawia.crazyae2addons.compat.GregTech;

import appeng.items.parts.PartItem;

public class GTDataExtractorPartItem extends PartItem<GTDataExtractorPart> {
    public GTDataExtractorPartItem(Properties properties) {
        super(properties, GTDataExtractorPart.class, GTDataExtractorPart::new);
    }
}