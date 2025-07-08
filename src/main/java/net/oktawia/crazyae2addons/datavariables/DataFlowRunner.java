package net.oktawia.crazyae2addons.datavariables;

import java.util.*;

public class DataFlowRunner {

    private final List<IFlowNode> allNodes;
    private final Map<IFlowNode, Map<String, DataValue<?>>> inputBuffers = new HashMap<>();

    public DataFlowRunner(List<IFlowNode> allNodes) {
        this.allNodes = allNodes;
    }

    public void run() {
        for (IFlowNode node : allNodes) {
            if (node.getExpectedInputs().isEmpty()) {
                tryExecute(node, "start", Map.of());
            }
        }
    }

    public void receiveInput(IFlowNode node, String inputName, DataValue<?> value) {
        inputBuffers
                .computeIfAbsent(node, n -> new HashMap<>())
                .put(inputName, value);

        tryExecute(node, inputName, inputBuffers.get(node));
    }

    private void tryExecute(IFlowNode node, String lastInput, Map<String, DataValue<?>> currentInputs) {
        Map<String, DataType> expected = node.getExpectedInputs();

        if (expected.isEmpty()) {
            Map<String, FlowResult> results = node.execute(lastInput, Map.of());

            for (Map.Entry<String, FlowResult> entry : results.entrySet()) {
                FlowResult res = entry.getValue();
                IFlowNode next = res.nextNode();

                if (next != null) {
                    String inputName = findNextAvailableInput(next, res.value());
                    if (inputName != null) {
                        receiveInput(next, inputName, res.value());
                    }
                }
            }

            return;
        }

        if (currentInputs == null) {
            return;
        }

        boolean ready = expected.keySet().stream().allMatch(currentInputs::containsKey);

        if (!ready) {
            return;
        }

        inputBuffers.remove(node);

        Map<String, FlowResult> results = node.execute(lastInput, currentInputs);

        for (Map.Entry<String, FlowResult> entry : results.entrySet()) {
            FlowResult res = entry.getValue();
            IFlowNode next = res.nextNode();

            if (next != null) {
                String inputName = findNextAvailableInput(next, res.value());
                if (inputName != null) {
                    receiveInput(next, inputName, res.value());
                }
            }
        }
    }

    private String findNextAvailableInput(IFlowNode node, DataValue<?> value) {
        Map<String, DataType> expected = node.getExpectedInputs();
        Map<String, DataValue<?>> current = inputBuffers.getOrDefault(node, Map.of());

        for (Map.Entry<String, DataType> entry : expected.entrySet()) {
            String inputName = entry.getKey();
            DataType expectedType = entry.getValue();

            if (!current.containsKey(inputName) && expectedType == value.getType()) {
                return inputName;
            }
        }

        return null;
    }

}
