package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.RightClickProviderPart;

public class RightClickProviderPartItem extends PartItem<RightClickProviderPart> {
    public RightClickProviderPartItem(Properties properties) {
        super(properties, RightClickProviderPart.class, RightClickProviderPart::new);
    }
}