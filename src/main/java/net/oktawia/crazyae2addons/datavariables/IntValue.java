package net.oktawia.crazyae2addons.datavariables;

public record IntValue(Integer value) implements DataValue<Integer> {

    @Override
    public DataType getType() {
        return DataType.INT;
    }

    @Override
    public Integer getRaw() {
        return value;
    }
}