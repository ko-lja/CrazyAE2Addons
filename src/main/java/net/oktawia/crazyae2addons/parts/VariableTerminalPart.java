package net.oktawia.crazyae2addons.parts;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.MEDataControllerBE;
import net.oktawia.crazyae2addons.menus.RedstoneTerminalMenu;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VariableTerminalPart extends AbstractDisplayPart implements IUpgradeableObject {
    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/redstone_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/redstone_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    public static String hexId = randomHexId();

    public VariableTerminalPart(IPartItem<?> partItem) {
        super(partItem, false);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!super.onPartActivate(player, hand, pos) && !this.isClientSide()) {
            MenuOpener.open(CrazyMenuRegistrar.VARIABLE_TERMINAL_MENU.get(), player, MenuLocators.forPart(this));
        }
        return true;
    }

    public Optional<MEDataControllerBE> findController() {
        if (this.getMainNode() != null && this.getMainNode().getGrid() != null) {
            var controllers = this.getMainNode().getGrid().getMachines(MEDataControllerBE.class);
            return controllers.stream().findFirst();
        }
        return Optional.empty();
    }

    public static String randomHexId() {
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int val = rand.nextInt(16);
            sb.append(Integer.toHexString(val).toUpperCase());
        }
        return sb.toString();
    }

    @Override
    public IPartModel getStaticModels() {
        if (!this.getMainNode().isActive()) {
            return MODELS_OFF;
        }
        return this.getMainNode().isPowered() ? MODELS_HAS_CHANNEL : MODELS_ON;
    }
}
