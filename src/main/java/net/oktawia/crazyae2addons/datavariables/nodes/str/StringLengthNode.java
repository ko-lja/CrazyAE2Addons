package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringLengthNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public StringLengthNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var in = inputs.get("in");
        if (in == null || in.getType() != DataType.STRING) return Map.of();

        int len = ((StringValue) in).getRaw().length();
        return Map.of("out", new FlowResult(new IntValue(len), next));
    }

    @Override public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.STRING);
    }

    @Override public String getType() {
        return "string_length";
    }
}
