package net.oktawia.crazyae2addons.datavariables.nodes.integer;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class IntConstNode implements IFlowNode {

    private final String id;
    private final IntValue value;
    private final IFlowNode next;

    public IntConstNode(String id, int value, IFlowNode next) {
        this.id = id;
        this.value = new IntValue(value);
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        return Map.of("out", new FlowResult(value, next));
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of();
    }

    @Override
    public String getType() {
        return "int_const";
    }
}
