package net.oktawia.crazyae2addons.datavariables.nodes.bool;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class BoolNotNode implements IFlowNode {

    private final String id;
    private final IFlowNode onTrue;
    private final IFlowNode onFalse;

    public BoolNotNode(String id, IFlowNode onTrue, IFlowNode onFalse) {
        this.id = id;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var in = inputs.get("in");

        if (in == null || in.getType() != DataType.BOOL) {
            return Map.of();
        }

        boolean result = !(Boolean) in.getRaw();
        return Map.of(
            result ? "true" : "false",
            new FlowResult(new BoolValue(result), result ? onTrue : onFalse)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.BOOL);
    }

    @Override
    public String getType() {
        return "bool_not";
    }
}
