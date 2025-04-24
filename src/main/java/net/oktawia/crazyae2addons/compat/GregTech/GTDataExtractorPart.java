package net.oktawia.crazyae2addons.compat.GregTech;

import appeng.api.parts.IPartItem;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.network.DataValuesPacket;
import net.oktawia.crazyae2addons.network.NetworkHandler;
import net.oktawia.crazyae2addons.parts.DataExtractorPart;

import java.lang.reflect.Field;

public class GTDataExtractorPart extends DataExtractorPart {
    public GTDataExtractorPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void extractPossibleData(){
        if (this.target == null){
            this.target = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
        }
        if (this.target instanceof MetaMachineBlockEntity){
            var gtMachine = SimpleTieredMachine.getMachine(getLevel(), target.getBlockPos());
            if (gtMachine == null) return;
            var machineClass = gtMachine.getClass();
            boolean useLogic = false;
            Field field;
            RecipeLogic recLogic = null;

            try {
                field = machineClass.getSuperclass().getDeclaredField("recipeLogic");
                field.setAccessible(true);
                try {
                    recLogic = (RecipeLogic) field.get(gtMachine);
                    useLogic = true;
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            if (useLogic){
                this.available = extractNumericInfo(recLogic);
            } else {
                this.available = extractNumericInfo(gtMachine);
            }
            RecipeLogic finalRecLogic = recLogic;
            boolean finalUseLogic = useLogic;
            if (finalUseLogic){
                this.resolveTarget = finalRecLogic;
            } else {
                this.resolveTarget = gtMachine;
            }
        } else {
            this.resolveTarget = this.target;
            this.available = extractNumericInfo(this.target);
        }
        if (!getLevel().isClientSide()) {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new DataValuesPacket(this.getBlockEntity().getBlockPos(), this.getSide(), this.available, this.selected, this.valueName));
        }
    }
}
