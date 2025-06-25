package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.misc.DataVariable;
import net.oktawia.crazyae2addons.parts.VariableTerminalPart;

import java.util.*;

public class VariableTerminalMenu extends AEBaseMenu {

    private final VariableTerminalPart terminal;

    @GuiSync(0)
    public String serializedVariables = "";

    public static final String ACTION_ADD = "addVariable";
    public static final String ACTION_REMOVE = "removeVariable";

    public VariableTerminalMenu(int id, Inventory inv, VariableTerminalPart host) {
        super(CrazyMenuRegistrar.VARIABLE_TERMINAL_MENU.get(), id, inv, host);
        createPlayerInventorySlots(inv);
        this.terminal = host;

        if (!isClientSide()) {
            this.loadVariables();
        }

        registerClientAction(ACTION_ADD, String.class, this::addVariable);
        registerClientAction(ACTION_REMOVE, String.class, this::removeVariable);
    }

    public void addVariable(String payload) {
        if (isClientSide()) {
            sendClientAction(ACTION_ADD, payload);
            return;
        }

        String[] parts = payload.split("=", 2);
        if (parts.length == 2) {
            String name = parts[0];
            try {
                int value = Integer.parseInt(parts[1]);
                terminal.findController().ifPresent(controller -> {
                    controller.addVariable(VariableTerminalPart.hexId, name, value, 0);
                    loadVariables();
                });
            } catch (NumberFormatException ignored) {}
        }
    }

    public void removeVariable(String name) {
        if (isClientSide()) {
            sendClientAction(ACTION_REMOVE, name);
            return;
        }

        terminal.findController().ifPresent(controller -> {
            String keyToRemove = null;

            for (Object obj : controller.variables.toStream().toList()) {
                Map.Entry<String, DataVariable> var = (Map.Entry<String, DataVariable>) obj;
                if (Objects.equals(var.getValue().name, name)) {
                    keyToRemove = var.getKey();
                    break;
                }
            }

            if (keyToRemove != null) {
                controller.variables.del(keyToRemove);
                loadVariables();
            }
        });
    }

    private void loadVariables() {
        terminal.findController().ifPresent(controller -> {
            StringBuilder sb = new StringBuilder();
            controller.variables.toStream().forEach(entry -> {
                Map.Entry<String, DataVariable> var = (Map.Entry<String, DataVariable>) entry;
                sb.append(var.getValue().name).append("=").append(var.getValue().value).append("|");
            });
            this.serializedVariables = sb.toString();
        });
    }

    public List<Map.Entry<String, Integer>> getVariableList() {
        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        if (serializedVariables == null || serializedVariables.isEmpty()) return result;

        String[] entries = serializedVariables.split("\\|");
        for (String entry : entries) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                try {
                    result.add(Map.entry(parts[0], Integer.parseInt(parts[1])));
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }

    public VariableTerminalPart getTerminal() {
        return terminal;
    }
}
