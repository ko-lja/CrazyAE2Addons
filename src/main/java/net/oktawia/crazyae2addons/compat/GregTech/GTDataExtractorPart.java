package net.oktawia.crazyae2addons.compat.GregTech;

import appeng.api.parts.IPartItem;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.network.DataValuesPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GTDataExtractorPart extends DataExtractorPart {
    public GTDataExtractorPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void extractPossibleData() {
        if (this.target == null) {
            this.target = getLevel()
                    .getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        }
        if (target == null){
            return;
        }

        List<String> data = new ArrayList<>();

        if (target instanceof MetaMachineBlockEntity) {
            var gtMachine = SimpleTieredMachine.getMachine(getLevel(), target.getBlockPos());
            if (gtMachine != null) {
                RecipeLogic recLogic = null;
                try {
                    Field f = gtMachine.getClass()
                            .getSuperclass()
                            .getDeclaredField("recipeLogic");
                    f.setAccessible(true);
                    recLogic = (RecipeLogic) f.get(gtMachine);
                } catch (Exception ignored) {}

                if (recLogic != null) {
                    data.addAll(extractNumericInfo(recLogic));
                    this.resolveTarget = recLogic;
                } else {
                    data.addAll(extractNumericInfo(gtMachine));
                    this.resolveTarget = gtMachine;
                }
            }
        } else {
            data.addAll(extractNumericInfo(target));
            this.resolveTarget = target;
        }

        if (target.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            data.add("percentFilled");
            data.add("fluidPercentFilled");
        } else {
            if (target.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                data.add("percentFilled");
            }
            if (target.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
                data.add("fluidPercentFilled");
            }
        }
        if (target.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
            data.add("storedEnergy");
        }

        if (!data.equals(this.available)) {
            this.available = data;
            if (selected >= available.size()) {
                selected = available.isEmpty() ? 0 : available.size() - 1;
            }
        }

        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new DataValuesPacket(
                            getBlockEntity().getBlockPos(),
                            getSide(),
                            this.available,
                            this.selected,
                            this.valueName
                    )
            );
        }
    }
}
