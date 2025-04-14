package net.oktawia.crazyae2addons.screens;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.MathParser;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.CrazyPatternModifierMenu;
import net.oktawia.crazyae2addons.menus.CrazyPatternMultiplierMenu;
import net.oktawia.crazyae2addons.misc.IconButton;
import org.jline.utils.Log;


public class CrazyPatternMultiplierScreen<C extends CrazyPatternMultiplierMenu> extends AEBaseScreen<C> {

    public IconButton confirm;
    public AETextField value;
    private boolean initialized = false;

    public CrazyPatternMultiplierScreen(CrazyPatternMultiplierMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);

        this.confirm = new IconButton(Icon.ENTER, this::modify);
        this.value = new AETextField(style, Minecraft.getInstance().font, 0,0,0,0);
        this.value.setBordered(false);
        this.value.setMaxLength(50);
        this.value.setPlaceholder(Component.literal("Multiplier"));
        this.widgets.add("confirm", this.confirm);
        this.widgets.add("value", this.value);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (!this.initialized){
            this.value.setValue(String.valueOf(getMenu().mult));
            this.initialized = true;
        }
    }

    public void modify(Button btn) {
        double evaled = 0;
        try {
            evaled = MathParser.parse(value.getValue());
            if (evaled <= 0){
                value.setTextColor(0xFF0000);
                Runnable col = () -> {value.setTextColor(0xFFFFFF);};
                Utils.asyncDelay(col, 1);
                return;
            }
        } catch (Exception ignored){
            value.setTextColor(0xFF0000);
            Runnable col = () -> {value.setTextColor(0xFFFFFF);};
            Utils.asyncDelay(col, 1);
            return;
        }
        value.setTextColor(0x00FF00);
        Runnable col = () -> {value.setTextColor(0xFFFFFF);};
        Utils.asyncDelay(col, 1);
        this.getMenu().modifyPatterns(evaled);
    }
}
