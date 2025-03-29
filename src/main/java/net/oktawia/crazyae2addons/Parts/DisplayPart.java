package net.oktawia.crazyae2addons.Parts;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.automation.PlaneModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.DisplayMenu;
import net.oktawia.crazyae2addons.network.DisplayNetworkHandler;
import net.oktawia.crazyae2addons.network.DisplayValuePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.player.Player;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import org.jline.utils.Log;

public class DisplayPart extends AEBasePart implements IGridTickable, MenuProvider {

    private static final PlaneModels MODELS = new PlaneModels("part/display_mon_off",
            "part/display_mon_on");

    public byte spin = 0; // 0-3
    public String textValue;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public DisplayPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1)
                .addService(IGridTickable.class, this);
        this.textValue = "";
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(0, 0, 15.5, 16, 16, 16);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 20, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!getLevel().isClientSide()) {
            DisplayNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new DisplayValuePacket(this.getBlockEntity().getBlockPos(), this.textValue, this.getSide(), this.spin));
        }
        return TickRateModulation.IDLE;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DisplayMenu(containerId, playerInventory, this);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    public boolean onPartActivate(Player p, InteractionHand hand, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(Menus.DISPLAY_MENU, p, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        if(extra.contains("textvalue")){
            this.textValue = extra.getString("textvalue");
        }
        if(extra.contains("spin")){
            this.spin = extra.getByte("spin");
        }
    }


    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        extra.putString("textvalue", this.textValue);
        extra.putByte("spin", this.spin);
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public final void onPlacement(Player player) {
        super.onPlacement(player);
        final byte rotation = (byte) (Mth.floor(player.getYRot() * 4F / 360F + 2.5D) & 3);
        if (getSide() == Direction.UP || getSide() == Direction.DOWN) {
            this.spin = rotation;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                              int combinedLightIn, int combinedOverlayIn) {
        if (!this.isPowered() || this.textValue.isEmpty()){
            return;
        }
        poseStack.pushPose();

        Direction facing = getSide();
        float rotation = 0.0F;
        float upRotation = 0.0F;
        switch (facing) {
            case SOUTH -> {
                rotation = 0.0F;
                poseStack.translate(0, 1, 0.5);
            }
            case WEST  -> {
                rotation = -90.0F;
                poseStack.translate(0.5, 1, 0);
            }
            case EAST  -> {
                rotation = 90.0F;
                poseStack.translate(0.5, 1, 1);
            }
            case NORTH -> {
                rotation = 180.0F;
                poseStack.translate(1, 1, 0.5);
            }
            case UP -> {
                upRotation = -90.0F;
                poseStack.translate(0, 0.5, 0);
            }
            case DOWN -> {
                upRotation = 90.0F;
                poseStack.translate(1, 0.5, 0);
            }
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(upRotation));
        if (upRotation != 0.0F){
            float theSpin = 0.0F;
            if (upRotation == 90.0F){
                switch (this.spin) {
                    case 0 -> {
                        theSpin = 0.0F;
                        poseStack.translate(-1, 1, 0);
                    }
                    case 1 -> {
                        theSpin = 90.0F;
                        poseStack.translate(-1, 0, 0);
                    }
                    case 2 -> {
                        theSpin = 180.0F;
                        poseStack.translate(0, 0, 0);
                    }
                    case 3 -> {
                        theSpin = -90.0F;
                        poseStack.translate(0, 1, 0);
                    }
                }
            } else {
                switch (this.spin) {
                    case 0 -> {
                        theSpin = 0.0F;
                        poseStack.translate(0, 0, 0);
                    }
                    case 1 -> {
                        theSpin = -90.0F;
                        poseStack.translate(1, 0, 0);
                    }
                    case 2 -> {
                        theSpin = 180.0F;
                        poseStack.translate(1, -1, 0);
                    }
                    case 3 -> {
                        theSpin = 90.0F;
                        poseStack.translate(0, -1, 0);
                    }
                }
            }
            poseStack.mulPose(Axis.ZP.rotationDegrees(theSpin));
        }

        var fr = Minecraft.getInstance().font;

        poseStack.translate(0, 0, 0.51);
        poseStack.scale(1.0f / 64.0f, -1.0f / 64.0f, 1.0f / 64.0f);


        String[] lines = this.textValue.split("&nl");
        String longestLine = Arrays.stream(lines)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
        int longestLineWidth = fr.width(longestLine);

        float baseScale = 1.0f / 64.0f;

        float fitScaleX = 64.0F / (longestLineWidth * baseScale);
        float fitScaleY = 64.0F / (lines.length * fr.lineHeight * baseScale);
        float finalScale;
        if (fitScaleY < fitScaleX){
            finalScale = baseScale * fitScaleY;
        } else {
            finalScale = baseScale * fitScaleX;
        }

        poseStack.scale(finalScale, finalScale, finalScale);
        for (int i = 0; i < lines.length; i++) {
            var text = Component.literal(lines[i]);
            fr.drawInBatch(
                text,
                (float) longestLineWidth / 2 - (float) fr.width(lines[i]) / 2,
                fr.lineHeight * i,
                0xFFFFFF,
                false,
                poseStack.last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                15728880);
        }
        poseStack.popPose();
    }
}

