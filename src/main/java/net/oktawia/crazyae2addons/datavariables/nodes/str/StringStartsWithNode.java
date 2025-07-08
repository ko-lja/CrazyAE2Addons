package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringStartsWithNode implements IFlowNode {

    private final String id;
    private final IFlowNode onTrue;
    private final IFlowNode onFalse;

    public StringStartsWithNode(String id, IFlowNode onTrue, IFlowNode onFalse) {
        this.id = id;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var str = inputs.get("text");
        var prefix = inputs.get("prefix");

        if (str == null || prefix == null || str.getType() != DataType.STRING || prefix.getType() != DataType.STRING)
            return Map.of();

        boolean result = ((StringValue) str).getRaw().startsWith(((StringValue) prefix).getRaw());

        return Map.of(
            result ? "true" : "false",
            new FlowResult(new BoolValue(result), result ? onTrue : onFalse)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("text", DataType.STRING, "prefix", DataType.STRING);
    }

    @Override public String getType() {
        return "string_startswith";
    }
}
