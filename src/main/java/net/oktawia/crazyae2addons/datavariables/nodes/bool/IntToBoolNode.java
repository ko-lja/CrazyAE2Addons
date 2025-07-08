package net.oktawia.crazyae2addons.datavariables.nodes.bool;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class IntToBoolNode implements IFlowNode {

    private final String id;
    private final IFlowNode onTrue;
    private final IFlowNode onFalse;

    public IntToBoolNode(String id, IFlowNode onTrue, IFlowNode onFalse) {
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
        DataValue<?> input = inputs.get("in");

        if (input == null || input.getType() != DataType.INT) return Map.of();

        int value = (Integer) input.getRaw();
        boolean result = value != 0;

        return Map.of(
            result ? "true" : "false",
            new FlowResult(new BoolValue(result), result ? onTrue : onFalse)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.INT);
    }

    @Override
    public String getType() {
        return "int_to_bool";
    }
}
