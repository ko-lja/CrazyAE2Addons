package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringConstNode implements IFlowNode {

    private final String id;
    private final String value;
    private final IFlowNode next;

    public StringConstNode(String id, String value, IFlowNode next) {
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
            "out", new FlowResult(new StringValue(value), next)
        );
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of();
    }

    @Override
    public String getType() {
        return "string_const";
    }
}
