package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class BoolToStringNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public BoolToStringNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        DataValue<?> input = inputs.get("in");
        if (input == null || input.getType() != DataType.BOOL) return Map.of();

        String str = String.valueOf(((BoolValue) input).getRaw());
        return Map.of("out", new FlowResult(new StringValue(str), next));
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.BOOL);
    }

    @Override
    public String getType() {
        return "bool_to_string";
    }
}
