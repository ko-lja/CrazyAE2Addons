package net.oktawia.crazyae2addons.datavariables.nodes.integer;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class IntMaxNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public IntMaxNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
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

        int result = Math.max(((IntValue) a).getRaw(), ((IntValue) b).getRaw());
        return Map.of("out", new FlowResult(new IntValue(result), next));
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("a", DataType.INT, "b", DataType.INT);
    }

    @Override
    public String getType() {
        return "int_max";
    }
}
