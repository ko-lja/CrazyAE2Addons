package net.oktawia.crazyae2addons.screens;

import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.BackgroundPanel;
import appeng.client.gui.widgets.VerticalButtonBar;
import com.google.gson.reflect.TypeToken;
import de.mari_023.ae2wtlib.wet.WETMenu;
import de.mari_023.ae2wtlib.wut.CycleTerminalButton;
import de.mari_023.ae2wtlib.wut.IUniversalTerminalCapable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.RedstoneTerminalMenu;
import net.oktawia.crazyae2addons.menus.WirelessRedstoneTerminalMenu;
import net.oktawia.crazyae2addons.misc.IconButton;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.oktawia.crazyae2addons.menus.WirelessRedstoneTerminalMenu.GSON;

public class WirelessRedstoneTerminalScreen<C extends WirelessRedstoneTerminalMenu> extends UpgradeableScreen<C> implements IUniversalTerminalCapable {

    private static final int EMITTERS_PER_PAGE = 4;
    private int currentPage = 0;

    private final List<Button> toggleButtons = new ArrayList<>();
    private final List<IconButton> stateIcons = new ArrayList<>();
    private boolean initialized = false;
    private String search = "";

    public WirelessRedstoneTerminalScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        if (this.getMenu().isWUT()) {
            this.addToLeftToolbar(new CycleTerminalButton((btn) -> this.cycleTerminal()));
        }
        this.widgets.add("singularityBackground", new BackgroundPanel(style.getImage("singularityBackground")));
        setupGui();
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized){
            refreshEmitters();
            initialized = true;
        }
        refreshEmitters();
    }

    private void setupGui() {
        var search = new AETextField(this.style, Minecraft.getInstance().font, 0,0,0,0);
        search.setBordered(false);
        search.setMaxLength(99);
        search.setPlaceholder(Component.literal("Search"));
        search.setResponder(newVal -> {
            this.search = newVal;
            this.getMenu().search(newVal);
        });
        this.widgets.add("search", search);

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
                    getMenu().search(this.search);

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

    @Override
    public void storeState() {}
}