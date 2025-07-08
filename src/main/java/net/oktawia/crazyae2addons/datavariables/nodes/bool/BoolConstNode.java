package net.oktawia.crazyae2addons.datavariables.nodes.bool;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class BoolConstNode implements IFlowNode {

    private final String id;
    private final boolean value;
    private final IFlowNode next;

    public BoolConstNode(String id, boolean value, IFlowNode next) {
        this.id = id;
        this.value = value;
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        return Map.of(
            "out", new FlowResult(new BoolValue(value), next)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of();
    }

    @Override
    public String getType() {
        return "bool_const";
    }
}
