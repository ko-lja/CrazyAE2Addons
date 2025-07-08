package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringSubstringNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public StringSubstringNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var str = inputs.get("in");
        var start = inputs.get("start");
        var end = inputs.get("end");

        if (str == null || start == null || end == null) return Map.of();
        if (str.getType() != DataType.STRING || start.getType() != DataType.INT || end.getType() != DataType.INT)
            return Map.of();

        String val = ((StringValue) str).getRaw();
        int s = ((IntValue) start).getRaw();
        int e = ((IntValue) end).getRaw();

        if (s < 0 || e > val.length() || s >= e) return Map.of();

        String out = val.substring(s, e);
        return Map.of("out", new FlowResult(new StringValue(out), next));
    }

    @Override public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.STRING, "start", DataType.INT, "end", DataType.INT);
    }

    @Override public String getType() {
        return "string_substring";
    }
}
