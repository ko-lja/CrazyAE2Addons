package net.oktawia.crazyae2addons.compat.DataExtracor;

import appeng.items.parts.PartItem;

public class CompatDataExtractorPartItem extends PartItem<CompatDataExtractorPart> {
    public CompatDataExtractorPartItem(Properties properties) {
        super(properties, CompatDataExtractorPart.class, CompatDataExtractorPart::new);
    }
}