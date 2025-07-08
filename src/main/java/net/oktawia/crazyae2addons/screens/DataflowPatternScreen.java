package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.DataflowPatternMenu;
import net.oktawia.crazyae2addons.misc.IconButton;
import net.oktawia.crazyae2addons.misc.NodeWidget;

import java.util.ArrayList;
import java.util.List;

public class DataflowPatternScreen<C extends DataflowPatternMenu> extends AEBaseScreen<C> {

    public List<String> nodes = new ArrayList<>();
    private AETextField searchBar;
    private List<String> filteredNodes = new ArrayList<>();
    private final int maxVisibleEntries = 12;
    private int scrollOffset = 0;
    private final int dropdownWidth = 121;
    private final int entryHeight = 12;
    private final List<NodeWidget> nodeWidgets = new ArrayList<>();
    private final boolean[] occupiedSlots = new boolean[8];
    private AETextField activeLabelEditor = null;
    private AETextField activeTargetEditor = null;
    private NodeWidget editingNode = null;
    private final int[][] slotPositions = new int[][] {
            {10, 25}, {70, 25}, {130, 25}, {190, 25},
            {10, 95}, {70, 95}, {130, 95}, {190, 95}
    };

    String[] boolNodes = {
            "Literal value", "AND", "==", "!=",
            "NOT", "OR", "XOR", "Sleep delay", "Int to Bool"
    };
    String[] intNodes = {
            "Literal value", "+", "-", "*", "/",
            "%", "==", "!=",
            ">", ">=", "<",
            "<=", "Max", "Min", "Sleep delay", "String to Int"
    };
    String[] stringNodes = {
            "Bool to String", "Int to String", "Entrypoint", "Variable reader", "Const value",
            "Concat", "Replace", "Substring", "Contains",
            "StartsWith", "EndsWith", "Length", "To lower case", "To upper case", "Sleep delay"
    };
    String[] outputNodes = {
            "Redstone emitter", "Set variable"
    };


    public DataflowPatternScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        for (String s : boolNodes) nodes.add("(B) " + s);
        for (String s : intNodes) nodes.add("(I) " + s);
        for (String s : stringNodes) nodes.add("(S) " + s);
        for (String s : outputNodes) nodes.add("(O) " + s);
        filteredNodes.addAll(nodes);
        setupGui();
    }

    public void setupGui() {
        searchBar = new AETextField(style, Minecraft.getInstance().font, 0, 0, 0, 0);
        searchBar.setBordered(false);
        searchBar.setResponder(this::onSearchChanged);
        this.widgets.add("search", searchBar);
        IconButton addNodeBtn = new IconButton(Icon.ENTER, b -> this.addNode());
        addNodeBtn.setTooltip(Tooltip.create(Component.literal("Add selected node")));
        this.widgets.add("addnode", addNodeBtn);
    }

    private void onSearchChanged(String input) {
        filteredNodes = nodes.stream()
                .filter(s -> s.toLowerCase().contains(input.toLowerCase()))
                .toList();
        scrollOffset = 0;
    }

    public void openEditorFor(NodeWidget widget) {
        this.editingNode = widget;

        int editorX = this.leftPos + 65;
        int editorY = this.topPos + 50;

        this.activeLabelEditor = new AETextField(style, font, editorX, editorY, 120, 16);
        this.activeLabelEditor.setBordered(false);
        this.activeLabelEditor.setTooltip(Tooltip.create(Component.literal("Name of this node")));
        this.activeLabelEditor.setPlaceholder(Component.literal("Name"));

        this.activeTargetEditor = new AETextField(style, font, editorX, editorY + 20, 120, 16);
        this.activeTargetEditor.setBordered(false);
        this.activeTargetEditor.setTooltip(Tooltip.create(Component.literal("Comma separated list of nodes that should run after this one")));
        this.activeTargetEditor.setPlaceholder(Component.literal("Name1, Name2..."));
    }

    public void closeEditor() {
        this.editingNode = null;
        this.activeLabelEditor = null;
        this.activeTargetEditor = null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (searchBar.isFocused()) {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();

            int x = searchBar.getX();
            int y = searchBar.getY() + 14;

            int shown = Math.min(maxVisibleEntries, filteredNodes.size() - scrollOffset);

            for (int i = 0; i < shown; i++) {
                int index = i + scrollOffset;
                if (index >= filteredNodes.size()) break;

                String entry = filteredNodes.get(index);
                int entryY = y + i * entryHeight;

                guiGraphics.fill(x, entryY, x + dropdownWidth, entryY + entryHeight, 0xFF202020);
                guiGraphics.drawString(font, entry, x + 4, entryY + 2, 0xFFFFFF);
            }

            pose.popPose();
        }

        if (editingNode != null) {
            guiGraphics.fill(leftPos + 55, topPos + 40, leftPos + 195, topPos + 90, 0xFF242424);
            activeLabelEditor.render(guiGraphics, mouseX, mouseY, partialTicks);
            activeTargetEditor.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBar.isFocused()) {
            int x = searchBar.getX();
            int y = searchBar.getY() + 14;

            for (int i = 0; i < Math.min(maxVisibleEntries, filteredNodes.size() - scrollOffset); i++) {
                int index = i + scrollOffset;
                int entryY = y + i * entryHeight;

                if (mouseX >= x && mouseX <= x + dropdownWidth && mouseY >= entryY && mouseY <= entryY + entryHeight) {
                    String selected = filteredNodes.get(index);
                    searchBar.setValue(selected);
                    return true;
                }
            }
        }

        if (editingNode != null) {
            boolean clickedLabel = activeLabelEditor.mouseClicked(mouseX, mouseY, button);
            boolean clickedTargets = activeTargetEditor.mouseClicked(mouseX, mouseY, button);

            activeLabelEditor.setFocused(clickedLabel);
            activeTargetEditor.setFocused(clickedTargets);

            if (clickedLabel || clickedTargets) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (searchBar.isFocused() && !filteredNodes.isEmpty()) {
            int maxOffset = Math.max(0, filteredNodes.size() - maxVisibleEntries);
            scrollOffset -= delta > 0 ? 1 : -1;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void addNode() {
        for (int i = 0; i < occupiedSlots.length; i++) {
            if (!occupiedSlots[i]) {
                int x = leftPos + slotPositions[i][0];
                int y = topPos + slotPositions[i][1];
                NodeWidget widget = new NodeWidget(x, y, 50, 60, searchBar.getValue(), (node) -> {}, style);
                this.nodeWidgets.add(widget);
                this.addRenderableWidget(widget);
                occupiedSlots[i] = true;
                return;
            }
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editingNode != null) {
            if (activeLabelEditor.charTyped(chr, modifiers)) return true;
            if (activeTargetEditor.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingNode != null) {
            if (activeLabelEditor.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (activeTargetEditor.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}