package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReadVariableNode implements IFlowNode {

    private final String id;
    private final String variableName;
    private final MEDataControllerBE controller;
    private final IFlowNode next;

    public ReadVariableNode(String id, String variableName, DataType expectedType, MEDataControllerBE controller, IFlowNode next) {
        this.id = id;
        this.variableName = variableName;
        this.controller = controller;
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        Optional<String> val = controller.variables
                .values()
                .stream()
                .filter(rec -> rec.name().equals(variableName))
                .map(MEDataControllerBE.VariableRecord::value)
                .filter(Objects::nonNull)
                .findFirst();

        return val.map(s -> Map.of("out", new FlowResult(new StringValue(s), next))).orElseGet(Map::of);

    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of();
    }

    @Override
    public String getType() {
        return "read_variable";
    }
}
