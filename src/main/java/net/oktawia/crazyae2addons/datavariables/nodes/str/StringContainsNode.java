package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringContainsNode implements IFlowNode {

    private final String id;
    private final IFlowNode onTrue;
    private final IFlowNode onFalse;

    public StringContainsNode(String id, IFlowNode onTrue, IFlowNode onFalse) {
        this.id = id;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var haystack = inputs.get("text");
        var needle = inputs.get("contains");

        if (haystack == null || needle == null ||
            haystack.getType() != DataType.STRING || needle.getType() != DataType.STRING) {
            return Map.of();
        }

        String value = ((StringValue) haystack).getRaw();
        String check = ((StringValue) needle).getRaw();
        boolean result = value.contains(check);

        return Map.of(
                result ? "true" : "false",
                new FlowResult(new BoolValue(result), result ? onTrue : onFalse)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("text", DataType.STRING, "contains", DataType.STRING);
    }

    @Override public String getType() {
        return "string_contains";
    }
}