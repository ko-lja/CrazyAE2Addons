package net.oktawia.crazyae2addons.datavariables;

public interface DataValue<T> {
    DataType getType();
    T getRaw();
}