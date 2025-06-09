package net.oktawia.crazyae2addons.compat.CC;

import appeng.items.parts.PartItem;

public class CCDataExtractorPartItem extends PartItem<CCDataExtractorPart> {
    public CCDataExtractorPartItem(Properties properties) {
        super(properties, CCDataExtractorPart.class, CCDataExtractorPart::new);
    }
}