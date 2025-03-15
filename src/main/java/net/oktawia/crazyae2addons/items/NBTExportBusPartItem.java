package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.Parts.EntityTickerPart;
import net.oktawia.crazyae2addons.Parts.NBTExportBusPart;

public class NBTExportBusPartItem extends PartItem<NBTExportBusPart> {
    public NBTExportBusPartItem(Properties properties) {
        super(properties, NBTExportBusPart.class, NBTExportBusPart::new);
    }
}