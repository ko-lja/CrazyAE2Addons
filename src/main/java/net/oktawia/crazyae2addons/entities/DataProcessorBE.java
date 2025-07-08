package net.oktawia.crazyae2addons.entities;

import appeng.api.networking.GridFlags;
import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.hooks.VisualStateSaving;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.oktawia.crazyae2addons.datavariables.DataFlowRunner;
import net.oktawia.crazyae2addons.datavariables.IFlowNode;
import net.oktawia.crazyae2addons.datavariables.nodes.bool.BoolConstNode;
import net.oktawia.crazyae2addons.datavariables.nodes.bool.BoolEqualsNode;
import net.oktawia.crazyae2addons.datavariables.nodes.bool.BoolNotEqualsNode;
import net.oktawia.crazyae2addons.datavariables.nodes.bool.IntToBoolNode;
import net.oktawia.crazyae2addons.datavariables.nodes.integer.StringToIntNode;
import net.oktawia.crazyae2addons.datavariables.nodes.output.SetRedstoneEmitterNode;
import net.oktawia.crazyae2addons.datavariables.nodes.str.StringConstNode;
import net.oktawia.crazyae2addons.datavariables.nodes.str.StringVariableProviderNode;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.interfaces.VariableMachine;
import net.oktawia.crazyae2addons.parts.RedstoneEmitterPart;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

public class DataProcessorBE extends AENetworkBlockEntity implements VariableMachine {

    public String identifier = randomHexId();

    public DataProcessorBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.DATA_PROCESSOR_BE.get(), pos, blockState);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(2).setVisualRepresentation(
                new ItemStack(CrazyBlockRegistrar.DATA_PROCESSOR_BLOCK.get())
        );
    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) sb.append(Integer.toHexString(rand.nextInt(16)).toUpperCase());
        return sb.toString();
    }

    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("ident")){
            this.identifier = data.getString("ident");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putString("ident", this.identifier);
    }

    @Override
    public String getId() {
        return this.identifier;
    }

    @Override
    public void notifyVariable(String name, String value, MEDataControllerBE db) {
        IFlowNode setRedstone = new SetRedstoneEmitterNode(
                "set_rs",
                getMainNode().getGrid().getMachines(RedstoneEmitterPart.class).stream().toList()
        );

        IFlowNode stringConst = new StringConstNode("const_str", "ABCD", setRedstone);
        IFlowNode boolEquals = new BoolEqualsNode("bool_equals", setRedstone, null);
        IFlowNode constTrue = new BoolConstNode("const_true", true, boolEquals);
        IFlowNode intToBool = new IntToBoolNode("int_to_bool", boolEquals, null);
        IFlowNode strToInt = new StringToIntNode("str_to_int", intToBool);
        IFlowNode entry = new StringVariableProviderNode("entry", "TEST", db, strToInt);

        List<IFlowNode> nodes = List.of(
                entry,
                strToInt,
                intToBool,
                constTrue,
                boolEquals,
                stringConst,
                setRedstone
        );

        DataFlowRunner runner = new DataFlowRunner(nodes);
        runner.run();
    }

    @Override
    public void onReady(){
        super.onReady();
        if (getMainNode().getGrid() == null) return;
        getMainNode()
                .getGrid()
                .getMachines(MEDataControllerBE.class)
                .stream()
                .findAny()
                .ifPresent(db -> db.registerNotification(this.identifier, "TEST", this.identifier, this.getClass()));
    }
}
