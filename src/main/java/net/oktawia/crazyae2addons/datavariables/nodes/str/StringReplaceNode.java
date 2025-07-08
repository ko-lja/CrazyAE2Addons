package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringReplaceNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public StringReplaceNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var text = inputs.get("text");
        var from = inputs.get("from");
        var to = inputs.get("to");

        if (text == null || from == null || to == null) return Map.of();
        if (text.getType() != DataType.STRING || from.getType() != DataType.STRING || to.getType() != DataType.STRING)
            return Map.of();

        String replaced = ((StringValue) text).getRaw()
                .replace(((StringValue) from).getRaw(), ((StringValue) to).getRaw());

        return Map.of("out", new FlowResult(new StringValue(replaced), next));
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("text", DataType.STRING, "from", DataType.STRING, "to", DataType.STRING);
    }

    @Override public String getType() {
        return "string_replace";
    }
}
