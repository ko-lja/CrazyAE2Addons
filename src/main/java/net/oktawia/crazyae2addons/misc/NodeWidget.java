package net.oktawia.crazyae2addons.misc;

import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.util.FormattedCharSequence;
import net.oktawia.crazyae2addons.screens.DataflowPatternScreen;

import java.util.List;
import java.util.function.Consumer;

public class NodeWidget extends AbstractWidget {
    private final Button editButton;
    private boolean editing = false;

    public NodeWidget(int x, int y, int width, int height, String label, Consumer<NodeWidget> onEditClick, ScreenStyle style) {
        super(x, y, width, height, Component.literal(label));

        this.editButton = Button.builder(Component.literal("⚙"), b -> {
            if (!editing) {
               if (Minecraft.getInstance().screen instanceof DataflowPatternScreen<?> patternScreen) patternScreen.openEditorFor(this);
               editing = true;
            } else {
                if (Minecraft.getInstance().screen instanceof DataflowPatternScreen<?> patternScreen) patternScreen.closeEditor();
                editing = false;
            }
        }).bounds(x + width - 14, y + 2, 12, 12).tooltip(Tooltip.create(Component.literal("Edit node"))).build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF303030);

        var font = Minecraft.getInstance().font;
        guiGraphics.drawCenteredString(font, "", getX() + getWidth() / 2, getY(), 0xFFFFFF);
        guiGraphics.drawString(font, "", getX(), getY(), 0xFFFFFF, false);
        guiGraphics.drawString(font, "", getX(), getY(), 0xFFFFFF, false);

        guiGraphics.blit(AbstractWidget.WIDGETS_LOCATION, getX() + getWidth() - 12, getY() + 2, 0, 66, 10, 10);

        int maxTextWidth = getWidth() - 6;
        List<FormattedCharSequence> lines = font.split(Component.literal(getMessage().getString()), maxTextWidth);
        int totalHeight = lines.size() * font.lineHeight;

        int startY = getY() + (getHeight() - totalHeight) / 2;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineWidth = font.width(line);
            int x = getX() + (getWidth() - lineWidth) / 2;
            int y = startY + i * font.lineHeight;
            guiGraphics.drawString(font, line, x, y, 0xFFFFFF, false);
        }
        editButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = editButton.mouseClicked(mouseX, mouseY, button);
        return clicked || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
