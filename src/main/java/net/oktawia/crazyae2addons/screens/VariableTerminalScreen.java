package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.VariableTerminalMenu;
import net.oktawia.crazyae2addons.misc.IconButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariableTerminalScreen<C extends VariableTerminalMenu> extends AEBaseScreen<C> {

    private static final int VARS_PER_PAGE = 4;
    private int currentPage = 0;
    private String search = "";

    private final List<Button> deleteButtons = new ArrayList<>();

    public VariableTerminalScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        setupGui();
    }

    private void setupGui() {
        var searchBox = new AETextField(this.style, Minecraft.getInstance().font, 0, 0, 0, 0);
        searchBox.setBordered(false);
        searchBox.setMaxLength(99);
        searchBox.setPlaceholder(Component.literal("Search"));
        searchBox.setResponder(newVal -> {
            this.search = newVal.toLowerCase();
            refreshVariables();
        });
        this.widgets.add("search", searchBox);

        IconButton prev = new IconButton(Icon.ARROW_LEFT, (x) -> {
            if (currentPage > 0) {
                currentPage--;
                refreshVariables();
            }
        });

        IconButton next = new IconButton(Icon.ARROW_RIGHT, (x) -> {
            int maxPage = (getFilteredVars(getMenu().getVariableList()).size() - 1) / VARS_PER_PAGE;
            if (currentPage < maxPage) {
                currentPage++;
                refreshVariables();
            }
        });

        this.widgets.add("prev_page", prev);
        this.widgets.add("next_page", next);

        for (int i = 0; i < VARS_PER_PAGE; i++) {
            final int rowIndex = i;

            Button delete = Button.builder(Component.literal("X"), btn -> {
                List<Map.Entry<String, Integer>> vars = getFilteredVars(getMenu().getVariableList());
                int index = currentPage * VARS_PER_PAGE + rowIndex;
                if (index < vars.size()) {
                    getMenu().removeVariable(vars.get(index).getKey());
                }
            }).build();

            this.widgets.add("delete_" + i, delete);
            deleteButtons.add(delete);
        }

        var nameField = new AETextField(this.style, Minecraft.getInstance().font, 0, 0, 0, 0);
        var valueField = new AETextField(this.style, Minecraft.getInstance().font, 0, 0, 0, 0);
        nameField.setBordered(false);
        valueField.setBordered(false);
        nameField.setMaxLength(32);
        valueField.setMaxLength(10);
        nameField.setPlaceholder(Component.literal("name"));
        valueField.setPlaceholder(Component.literal("value"));

        this.widgets.add("name_input", nameField);
        this.widgets.add("value_input", valueField);

        IconButton addBtn = new IconButton(Icon.ENTER, (b) -> {
            try {
                String name = nameField.getValue().trim();
                int value = Integer.parseInt(valueField.getValue().trim());
                getMenu().addVariable(name + "=" + value);
                nameField.setValue("");
                valueField.setValue("");
            } catch (NumberFormatException ignored) {}
        });
        this.widgets.add("add_btn", addBtn);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        refreshVariables();
    }

    private void refreshVariables() {
        List<Map.Entry<String, Integer>> vars = getFilteredVars(getMenu().getVariableList());

        for (int i = 0; i < VARS_PER_PAGE; i++) {
            int index = currentPage * VARS_PER_PAGE + i;
            Button delete = deleteButtons.get(i);
            String labelId = "label_" + i;

            if (index < vars.size()) {
                var entry = vars.get(index);
                setTextContent(labelId, Component.literal(entry.getKey() + " = " + entry.getValue()));
                delete.visible = true;
            } else {
                setTextContent(labelId, Component.empty());
                delete.visible = false;
            }
        }
    }

    private @NotNull List<Map.Entry<String, Integer>> getFilteredVars(List<Map.Entry<String, Integer>> input) {
        if (search.isBlank()) return input;
        return input.stream()
                .filter(e -> e.getKey().toLowerCase().contains(search))
                .toList();
    }
}
