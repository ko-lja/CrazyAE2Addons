package net.oktawia.crazyae2addons.datavariables.nodes.output;

import net.oktawia.crazyae2addons.datavariables.*;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;

import java.security.SecureRandom;
import java.util.Map;

public class SetVariableNode implements IFlowNode {

    private final String id;
    private final String targetName;
    private final MEDataControllerBE controller;

    public SetVariableNode(String id, String targetName, MEDataControllerBE controller) {
        this.id = id;
        this.targetName = targetName;
        this.controller = controller;
    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) sb.append(Integer.toHexString(rand.nextInt(16)).toUpperCase());
        return sb.toString();
    }

    @Override
    public String getId() { return id; }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        DataValue<?> value = inputs.get("in");
        if (value == null) return Map.of();

        controller.addVariable(randomHexId(), this.getClass(), randomHexId(), targetName, (String) value.getRaw());

        return Map.of();
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of("in", DataType.STRING);
    }

    @Override
    public String getType() {
        return "set_variable";
    }
}
