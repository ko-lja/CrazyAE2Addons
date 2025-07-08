package net.oktawia.crazyae2addons.datavariables;

public record StringValue(String value) implements DataValue<String> {

    @Override
    public DataType getType() {
        return DataType.STRING;
    }

    @Override
    public String getRaw() {
        return value;
    }
}