package net.oktawia.crazyae2addons.datavariables.nodes.integer;

import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;

public class StringToIntNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public StringToIntNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var input = inputs.get("in");

        if (input == null || input.getType() != DataType.STRING) return Map.of();

        try {
            double parsed = Double.parseDouble((String) input.getRaw());
            int result = (int) parsed;
            return Map.of("out", new FlowResult(new IntValue(result), next));
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.STRING);
    }

    @Override
    public String getType() {
        return "string_to_int";
    }
}