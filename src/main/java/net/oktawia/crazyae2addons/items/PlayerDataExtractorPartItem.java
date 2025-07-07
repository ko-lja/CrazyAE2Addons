package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.PlayerDataExtractorPart;

public class PlayerDataExtractorPartItem extends PartItem<PlayerDataExtractorPart> {
    public PlayerDataExtractorPartItem(Properties properties) {
        super(properties, PlayerDataExtractorPart.class, PlayerDataExtractorPart::new);
    }
}