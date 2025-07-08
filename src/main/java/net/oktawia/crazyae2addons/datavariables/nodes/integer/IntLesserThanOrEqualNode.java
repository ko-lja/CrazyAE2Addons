package net.oktawia.crazyae2addons.datavariables.nodes.integer;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class IntLesserThanOrEqualNode implements IFlowNode {

    private final String id;
    private final IFlowNode onTrue;
    private final IFlowNode onFalse;

    public IntLesserThanOrEqualNode(String id, IFlowNode onTrue, IFlowNode onFalse) {
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
        DataValue<?> a = inputs.get("a");
        DataValue<?> b = inputs.get("b");

        if (a == null || b == null || a.getType() != DataType.INT || b.getType() != DataType.INT)
            return Map.of();

        boolean result = ((IntValue) a).getRaw() <= ((IntValue) b).getRaw();
        return Map.of(
                result ? "true" : "false",
                new FlowResult(new BoolValue(result), result ? onTrue : onFalse)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("a", DataType.INT, "b", DataType.INT);
    }

    @Override
    public String getType() {
        return "int_lte";
    }
}
