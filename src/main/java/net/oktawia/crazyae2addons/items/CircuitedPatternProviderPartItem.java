package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.CircuitedPatternProviderPart;
import net.oktawia.crazyae2addons.parts.EntityTickerPart;

public class CircuitedPatternProviderPartItem extends PartItem<CircuitedPatternProviderPart> {
    public CircuitedPatternProviderPartItem(Properties properties) {
        super(properties, CircuitedPatternProviderPart.class, CircuitedPatternProviderPart::new);
    }
}