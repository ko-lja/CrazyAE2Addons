package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.RedstoneTerminalPart;
import net.oktawia.crazyae2addons.parts.VariableTerminalPart;

public class VariableTerminalPartItem extends PartItem<VariableTerminalPart> {
    public VariableTerminalPartItem(Properties properties) {
        super(properties, VariableTerminalPart.class, VariableTerminalPart::new);
    }
}