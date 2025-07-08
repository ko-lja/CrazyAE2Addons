package net.oktawia.crazyae2addons.datavariables.nodes.str;

import net.oktawia.crazyae2addons.datavariables.*;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;

import java.util.Map;
import java.util.Objects;

public class StringVariableProviderNode implements IFlowNode {

    private final String id;
    private final String variableName;
    private final MEDataControllerBE variableStorage;
    private final IFlowNode next;

    public StringVariableProviderNode(String id, String variableName, MEDataControllerBE variableStorage, IFlowNode next) {
        this.id = id;
        this.variableName = variableName;
        this.variableStorage = variableStorage;
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        for (var record : variableStorage.variables.values()) {
            if (Objects.equals(record.name(), variableName)) {
                return Map.of(
                        "out", new FlowResult(new StringValue(record.value()), next)
                );
            }
        }
        return Map.of();
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of();
    }

    @Override
    public String getType() {
        return "string_variable_provider";
    }
}
