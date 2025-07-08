package net.oktawia.crazyae2addons.datavariables.nodes.output;

import net.oktawia.crazyae2addons.datavariables.*;
import net.oktawia.crazyae2addons.parts.RedstoneEmitterPart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SetRedstoneEmitterNode implements IFlowNode {

    private final String id;
    private final List<RedstoneEmitterPart> emitterRegistry;

    public SetRedstoneEmitterNode(String id, List<RedstoneEmitterPart> emitterRegistry) {
        this.id = id;
        this.emitterRegistry = emitterRegistry;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        var nameVal = inputs.get("name");
        var stateVal = inputs.get("state");

        if (nameVal == null || stateVal == null) return Map.of();
        if (nameVal.getType() != DataType.STRING || stateVal.getType() != DataType.BOOL) return Map.of();

        String emitterName = (String) nameVal.getRaw();
        boolean state = (Boolean) stateVal.getRaw();

        emitterRegistry
                .stream()
                .filter(emitter -> Objects.equals(emitter.name, emitterName))
                .findAny().ifPresent(part -> part.setState(state));

        return Map.of();
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of(
            "name", DataType.STRING,
            "state", DataType.BOOL
        );
    }

    @Override
    public String getType() {
        return "set_redstone_emitter";
    }
}
