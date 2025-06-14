package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.CrazyPatternProviderPart;

public class CrazyPatternProviderPartItem extends PartItem<CrazyPatternProviderPart> {
    public CrazyPatternProviderPartItem(Properties properties) {
        super(properties, CrazyPatternProviderPart.class, CrazyPatternProviderPart::new);
    }
}