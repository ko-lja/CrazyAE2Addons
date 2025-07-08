package net.oktawia.crazyae2addons.interfaces;

import net.oktawia.crazyae2addons.entities.MEDataControllerBE;

public interface VariableMachine {
    public String getId();
    public void notifyVariable(String name, String value, MEDataControllerBE db);
}
