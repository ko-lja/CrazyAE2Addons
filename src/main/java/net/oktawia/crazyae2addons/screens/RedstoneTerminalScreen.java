package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.RedstoneTerminalMenu;
import net.oktawia.crazyae2addons.misc.IconButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.oktawia.crazyae2addons.menus.RedstoneTerminalMenu.GSON;

public class RedstoneTerminalScreen<C extends RedstoneTerminalMenu> extends AEBaseScreen<C> {

    private static final int EMITTERS_PER_PAGE = 4;
    private int currentPage = 0;
    private boolean initialized = false;

    private final List<Button> toggleButtons = new ArrayList<>();
    private final List<IconButton> stateIcons = new ArrayList<>();

    public RedstoneTerminalScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        setupGui();
    }

    @Override
    public void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized){
            refreshEmitters();
            initialized = true;
        }
    }


    private void setupGui() {
        IconButton prev = new IconButton(Icon.ARROW_LEFT, (x) -> {
            if (currentPage > 0) {
                currentPage--;
                refreshEmitters();
            }
        });

        IconButton next = new IconButton(Icon.ARROW_RIGHT, (x) -> {
            int maxPage = (getEmitters().size() - 1) / EMITTERS_PER_PAGE;
            if (currentPage < maxPage) {
                currentPage++;
                refreshEmitters();
            }
        });

        this.widgets.add("prev_page", prev);
        this.widgets.add("next_page", next);

        for (int i = 0; i < EMITTERS_PER_PAGE; i++) {
            final int rowIndex = i;

            IconButton state = new IconButton(Icon.REDSTONE_LOW, x -> {});
            state.active = false;

            Button toggle = Button.builder(Component.literal(""), btn -> {
                int index = currentPage * EMITTERS_PER_PAGE + rowIndex;
                List<RedstoneTerminalMenu.EmitterInfo> emitters = getEmitters();
                if (index < emitters.size()) {
                    RedstoneTerminalMenu.EmitterInfo emitter = emitters.get(index);
                    getMenu().toggle(emitter.name());

                    IconButton stateBtn = stateIcons.get(rowIndex);
                    stateBtn.icon = (stateBtn.icon == Icon.REDSTONE_LOW) ? Icon.REDSTONE_HIGH : Icon.REDSTONE_LOW;
                }
            }).build();

            this.widgets.add("toggle_" + rowIndex, toggle);
            this.widgets.add("pulse_" + rowIndex, state);

            toggleButtons.add(toggle);
            stateIcons.add(state);
        }
    }

    private void refreshEmitters() {
        List<RedstoneTerminalMenu.EmitterInfo> emitters = getEmitters();

        for (int i = 0; i < EMITTERS_PER_PAGE; i++) {
            int index = currentPage * EMITTERS_PER_PAGE + i;
            Button toggle = toggleButtons.get(i);
            IconButton state = stateIcons.get(i);

            if (index < emitters.size()) {
                RedstoneTerminalMenu.EmitterInfo emitter = emitters.get(index);
                Component name = Component.literal(emitter.name());

                this.setTextContent("label_" + i, name);
                toggle.setMessage(Component.empty());
                toggle.visible = true;
                state.visible = true;

                state.icon = emitter.active() ? Icon.REDSTONE_HIGH : Icon.REDSTONE_LOW;
            } else {
                toggle.setMessage(Component.empty());
                toggle.visible = false;
                state.visible = false;
                this.setTextContent("label_" + i, Component.empty());
            }
        }
    }

    private @NotNull List<RedstoneTerminalMenu.EmitterInfo> getEmitters() {
        return GSON.fromJson(getMenu().emitters, new TypeToken<List<RedstoneTerminalMenu.EmitterInfo>>() {}.getType());
    }
}