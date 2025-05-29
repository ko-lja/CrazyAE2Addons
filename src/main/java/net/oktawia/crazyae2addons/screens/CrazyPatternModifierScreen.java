package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fml.ModList;
import net.oktawia.crazyae2addons.menus.CrazyPatternModifierMenu;
import net.oktawia.crazyae2addons.misc.IconButton;
import net.minecraft.client.gui.components.Button;


public class CrazyPatternModifierScreen<C extends CrazyPatternModifierMenu> extends AEBaseScreen<C> {

    public IconButton nbt;
    public IconButton circConfirm;
    public AETextField circ;

    public CrazyPatternModifierScreen(CrazyPatternModifierMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        setupGui();
        this.widgets.add("circ", circ);
    }

    private void setupGui(){
        this.nbt = new IconButton(Icon.ENTER, this::changeNbt);
        this.circConfirm = new IconButton(Icon.ENTER, this::modifyCirc);

        this.widgets.add("nbt", this.nbt);
        this.widgets.add("confirmcirc", this.circConfirm);
        circ = new AETextField(
                style, Minecraft.getInstance().font, 0, 0, 0, 0
        );
        circ.setBordered(false);
        circ.setMaxLength(2);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        setTextContent("info1", Component.literal(getMenu().textNBT));
        if (ModList.get().isLoaded("gtceu")){
            setTextContent("info2", Component.literal(getMenu().textCirc));
        } else {
            setTextContent("info2", Component.literal("GregTech not detected"));
        }
    }

    public void changeNbt(Button btn) {
        this.getMenu().changeNBT();
    }

    public void modifyCirc(Button btn){
        if (ModList.get().isLoaded("gtceu")){
            if (circ.getValue().isEmpty()){
                this.getMenu().changeCircuit(-1);
            } else if ((circ.getValue().chars().allMatch(Character::isDigit) && !circ.getValue().isEmpty() && Integer.parseInt(circ.getValue()) <= 32)){
                this.getMenu().changeCircuit(Integer.parseInt(circ.getValue()));
            }
        }
    }
}
