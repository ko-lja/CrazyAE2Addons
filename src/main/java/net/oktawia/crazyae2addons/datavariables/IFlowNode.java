package net.oktawia.crazyae2addons.datavariables;

import java.util.Map;

public interface IFlowNode {

    String getId();

    Map<String, FlowResult> execute(String where, Map<String, DataValue<?>> inputs);

    Map<String, DataType> getExpectedInputs();

    String getType();
}

