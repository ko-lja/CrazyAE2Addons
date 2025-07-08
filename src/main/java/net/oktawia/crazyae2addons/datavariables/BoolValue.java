package net.oktawia.crazyae2addons.datavariables;

public record BoolValue(Boolean value) implements DataValue<Boolean> {

    @Override
    public DataType getType() {
        return DataType.BOOL;
    }

    @Override
    public Boolean getRaw() {
        return value;
    }
}