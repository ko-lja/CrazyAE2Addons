package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringConcatNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public StringConcatNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        DataValue<?> a = inputs.get("a");
        DataValue<?> b = inputs.get("b");

        if (a == null || b == null || a.getType() != DataType.STRING || b.getType() != DataType.STRING) return Map.of();

        String out = ((StringValue) a).getRaw() + ((StringValue) b).getRaw();
        return Map.of("out", new FlowResult(new StringValue(out), next));
    }

    @Override public Map<String, DataType> getExpectedInputs() {
        return Map.of("a", DataType.STRING, "b", DataType.STRING);
    }

    @Override public String getType() {
        return "string_concat";
    }
}
