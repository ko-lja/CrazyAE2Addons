package net.oktawia.crazyae2addons.parts;

import appeng.api.parts.IPartItem;
import appeng.parts.AEBasePart;


public abstract class NotifyablePart extends AEBasePart {
    public NotifyablePart(IPartItem<?> partItem) {
        super(partItem);
    }

    public abstract void doNotify(String name, Integer value);
}
