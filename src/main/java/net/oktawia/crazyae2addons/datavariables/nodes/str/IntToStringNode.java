package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class IntToStringNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public IntToStringNode(String id, IFlowNode next) {
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
        if (input == null || input.getType() != DataType.INT) return Map.of();

        String str = String.valueOf(((IntValue) input).getRaw());
        return Map.of("out", new FlowResult(new StringValue(str), next));
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.INT);
    }

    @Override
    public String getType() {
        return "int_to_string";
    }
}
