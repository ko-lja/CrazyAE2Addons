package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringEndsWithNode implements IFlowNode {

    private final String id;
    private final IFlowNode onTrue;
    private final IFlowNode onFalse;

    public StringEndsWithNode(String id, IFlowNode onTrue, IFlowNode onFalse) {
        this.id = id;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var str = inputs.get("text");
        var suffix = inputs.get("suffix");

        if (str == null || suffix == null || str.getType() != DataType.STRING || suffix.getType() != DataType.STRING)
            return Map.of();

        boolean result = ((StringValue) str).getRaw().endsWith(((StringValue) suffix).getRaw());

        return Map.of(
            result ? "true" : "false",
            new FlowResult(new BoolValue(result), result ? onTrue : onFalse)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("text", DataType.STRING, "suffix", DataType.STRING);
    }

    @Override public String getType() {
        return "string_endswith";
    }
}
