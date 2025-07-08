package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.menus.DataExtractorMenu;
import net.oktawia.crazyae2addons.menus.PlayerDataExtractorMenu;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerDataExtractorScreen<C extends PlayerDataExtractorMenu> extends AEBaseScreen<C> {

    public boolean initialized = false;
    public boolean initialized2 = false;
    public AbstractWidget btn0;
    public AbstractWidget btn1;
    public AbstractWidget btn2;
    public AbstractWidget btn3;
    public AbstractWidget btn4;
    public AETextField input;
    public AETextField delay;
    public Button playerButton;

    public PlayerDataExtractorScreen(
            C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        if (!this.initialized){
            setupGui();
            this.initialized = true;
        }
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        if (!initialized2){
            this.input.setValue(this.getMenu().valueName);
            this.delay.setValue(String.valueOf(this.getMenu().delay));
            var player = Minecraft.getInstance().level.getPlayerByUUID(UUID.fromString(getMenu().boundPlayer));
            if (player != null) {
                this.playerButton.setMessage(Component.literal("Bound to: " + player.getName().getString()));
            }
            renderPage(getMenu().page * 4, (getMenu().page + 1) * 4);
            initialized2 = true;
        }
        String selected;
        if (!getMenu().available.isEmpty()){
            selected = "Selected: " + Arrays.stream(getMenu().available.split("\\|")).toList().get(getMenu().selected);
        } else {
            selected = "Selected: ";
        }
        setTextContent("selectedValue", Component.literal(selected));
        if(getMenu().updateGui){
            updateGui();
            getMenu().updateGui = false;
        }
    }

    public void setupGui(){
        btn0 = Button.builder(Component.literal("0 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn1 = Button.builder(Component.literal("1 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn2 = Button.builder(Component.literal("2 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        btn3 = Button.builder(Component.literal("3 "), (btn) -> {
            setSelected(Integer.valueOf(Arrays.stream(btn.getMessage().getString().split(" ")).toList().get(0)));
        }).build();
        this.widgets.add("button0", btn0);
        this.widgets.add("button1", btn1);
        this.widgets.add("button2", btn2);
        this.widgets.add("button3", btn3);
        this.widgets.addButton("data", Component.literal("fetch"), (btn) -> {getMenu().getData(); updateGui();});
        this.widgets.addButton("down", Component.literal("<"), (btn) -> {
            int newPage = getMenu().page - 1;
            if (newPage >= 0 && pageHasData(newPage)) {
                getMenu().page = newPage;
                updateGui();
            }
        });
        this.widgets.addButton("up", Component.literal(">"), (btn) -> {
            int newPage = getMenu().page + 1;
            if (pageHasData(newPage)) {
                getMenu().page = newPage;
                updateGui();
            }
        });
        this.widgets.addButton("save", Component.literal("+"), (btn) -> {updateVariableName();});
        this.input = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.input.setPlaceholder(Component.literal("Variables Name"));
        this.input.setValue(getMenu().valueName);
        this.input.setMaxLength(999);
        this.input.setBordered(false);
        this.delay = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        this.delay.setPlaceholder(Component.literal("Delay"));
        this.delay.setTooltip(Tooltip.create(Component.literal("Read interval")));
        this.delay.setValue(String.valueOf(getMenu().delay));
        this.delay.setMaxLength(10);
        this.delay.setBordered(false);
        this.widgets.add("input", this.input);
        this.widgets.add("delay", this.delay);
        this.widgets.addButton("toggleMode", Component.literal(getMenu().playerMode ? "Closest player" : "Bind player"), (btn) -> {
            getMenu().togglePlayerMode();
            btn.setMessage(Component.literal(getMenu().playerMode ? "Closest player" : "Bind player"));
        });
        String playerName = "Unknown";
        this.playerButton = new Button.Builder(Component.literal("Bound to: " + playerName), (btn) -> {
            if (Minecraft.getInstance().player != null) {
                getMenu().bindPlayer(Minecraft.getInstance().player.getStringUUID());
                btn.setMessage(Component.literal("Bound to: " + Minecraft.getInstance().player.getName().getString()));
            }}).build();
        this.widgets.add("bindPlayer", this.playerButton);
    }

    public void renderPage(int start, int end) {
        try {
            setTextContent("selectedValue", Component.literal("Selected: " + Arrays.stream(getMenu().available.split(Pattern.quote("|"))).toList().get(getMenu().selected)));
        } catch (Exception ignored) {}

        String[] parts = getMenu().available.split("\\|");
        for (int i = start; i < end; i++) {
            int localIndex = i - start;
            if (i >= 0 && i < parts.length) {
                String label = i + " " + parts[i];
                getButton(localIndex).setMessage(Component.literal(label));
                getButton(localIndex).visible = true;
            } else {
                getButton(localIndex).visible = false;
            }
        }
    }

    private AbstractWidget getButton(int index) {
        return switch (index) {
            case 0 -> btn0;
            case 1 -> btn1;
            case 2 -> btn2;
            case 3 -> btn3;
            default -> btn0;
        };
    }

    private boolean pageHasData(int page) {
        String[] parts = getMenu().available.split("\\|");
        int start = page * 4;
        for (int i = start; i < start + 4; i++) {
            if (i < parts.length) return true;
        }
        return false;
    }


    public void setSelected(Integer what){
        if (what == -1){
            return;
        }
        getMenu().selected = what;
        getMenu().syncValue(what);
        updateGui();
    }

    public void updateGui(){
        setTextContent("selectedValue", Component.literal("Selected: " + Arrays.stream(getMenu().available.split("\\|")).toList().get(getMenu().selected)));
        renderPage(getMenu().page * 4, (getMenu().page + 1) * 4);
    }

    public static boolean isAscii(String input) {
        return input.chars().allMatch(c -> c <= 127);
    }

    public void updateVariableName(){
        String name = this.input.getValue();
        String delay = this.delay.getValue();
        if (isAscii(name) && !name.isEmpty() && delay.chars().allMatch(Character::isDigit)){
            name = name.toUpperCase();
            this.input.setTextColor(0x00FF00);
            Runnable setColorFunction = () -> this.input.setTextColor(0xFFFFFF);
            Utils.asyncDelay(setColorFunction, 1);
            getMenu().saveName(name);
            getMenu().saveDelay(delay.isEmpty() ? 0 : Integer.parseInt(delay));
        }
    }

}