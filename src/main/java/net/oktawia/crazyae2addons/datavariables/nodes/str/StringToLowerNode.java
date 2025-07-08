package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringToLowerNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public StringToLowerNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var in = inputs.get("in");
        if (in == null || in.getType() != DataType.STRING) return Map.of();

        String out = ((StringValue) in).getRaw().toLowerCase();
        return Map.of("out", new FlowResult(new StringValue(out), next));
    }

    @Override public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.STRING);
    }

    @Override public String getType() {
        return "string_lower";
    }
}
