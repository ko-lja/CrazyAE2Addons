package net.oktawia.crazyae2addons.mobstorage;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.RightClickProviderPart;

public class MobAnnihilationPlaneItem extends PartItem<MobAnnihilationPlane> {
    public MobAnnihilationPlaneItem(Properties properties) {
        super(properties, MobAnnihilationPlane.class, MobAnnihilationPlane::new);
    }
}