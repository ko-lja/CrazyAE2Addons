package net.oktawia.crazyae2addons.datavariables.nodes.bool;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.oktawia.crazyae2addons.datavariables.*;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BoolSleepNode implements IFlowNode {

    private final String id;
    private final IFlowNode next;

    public BoolSleepNode(String id, IFlowNode next) {
        this.id = id;
        this.next = next;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs) {
        DataValue<?> duration = inputs.get("duration");
        DataValue<?> payload = inputs.get("in");

        if (duration == null || duration.getType() != DataType.INT || payload == null)
            return Map.of();

        int ticks = ((IntValue) duration).getRaw();
        if (ticks <= 0) return Map.of("out", new FlowResult(payload, next));

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Map.of();

        long delayMillis = ticks * 50L;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                server.execute(() -> {
                    if (next != null) {
                        new DataFlowRunner(java.util.List.of(next))
                                .receiveInput(next, "in", payload);
                    }
                });
            }
        }, delayMillis);

        return Map.of();
    }

    @Override
    public Map<String, DataType> getExpectedInputs() {
        return Map.of(
                "duration", DataType.INT,
                "in", DataType.BOOL
        );
    }

    @Override
    public String getType() {
        return "bool_sleep";
    }
}
