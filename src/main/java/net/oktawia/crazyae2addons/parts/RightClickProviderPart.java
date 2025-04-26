package net.oktawia.crazyae2addons.parts;

import appeng.api.config.*;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.core.definitions.AEItems;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.automation.UpgradeablePart;
import appeng.parts.p2p.P2PModels;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.Menus;
import net.oktawia.crazyae2addons.menus.RightClickProviderMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class RightClickProviderPart extends UpgradeablePart implements
        IUpgradeableObject, IGridTickable, InternalInventoryHost, MenuProvider {

    public AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private static final P2PModels MODELS = new P2PModels(
            new ResourceLocation(CrazyAddons.MODID, "part/rc_provider"));

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }
    public FakePlayer fakePlayer;
    public int waitingFor = 0;

    public RightClickProviderPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(8)
                .addService(IGridTickable.class,this);
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        var inv = this.inv;
        if (inv != InternalInventory.empty()) {
            var opt = extra.getCompound("inv");
            for (int x = 0; x < inv.size(); x++) {
                var item = opt.getCompound("item" + x);
                inv.setItemDirect(x, ItemStack.of(item));
            }
        }
    }


    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        var inv = this.inv;
        if (inv != InternalInventory.empty()) {
            final CompoundTag opt = new CompoundTag();
            for (int x = 0; x < inv.size(); x++) {
                final CompoundTag item = new CompoundTag();
                final ItemStack is = inv.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.save(item);
                }
                opt.put("item" + x, item);
            }
            extra.put("inv", opt);
        }
    }

    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public boolean onPartActivate(Player p, InteractionHand hand, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(Menus.RIGHT_CLICK_PROVIDER_MENU, p, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RightClickProviderMenu(containerId, playerInventory, this);
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(5, 5, 12, 11, 11, 13);
        bch.addBox(3, 3, 13, 13, 13, 14);
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public void upgradesChanged() {
        getHost().markForSave();
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    protected int getUpgradeSlots() {
        return 4;
    }


    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1,
                1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        waitingFor ++;
        if (waitingFor < 40 / Math.pow(2, getInstalledUpgrades(AEItems.SPEED_CARD))){
            return TickRateModulation.IDLE;
        }
        waitingFor = 0;
        if (getLevel().getServer() == null) return TickRateModulation.IDLE;
        var world = getLevel().getServer().getLevel(getLevel().dimension());
        if (this.fakePlayer == null){
            this.fakePlayer = FakePlayerFactory.get(Objects.requireNonNull(getLevel().getServer()).getLevel(getLevel().dimension()),
                    new GameProfile(UUID.randomUUID(), "[CrazyAE2Addons]"));
        }
        if (world == null) return TickRateModulation.IDLE;
        var pos = getBlockEntity().getBlockPos().relative(getSide());
        BlockState state = world.getBlockState(pos);
        Direction face = getSide();
        Vec3 hitVec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitVec, face, pos, false);
        if (!this.inv.getStackInSlot(0).isEmpty()){
            ItemStack stack = this.inv.getStackInSlot(0);
            UseOnContext context = new UseOnContext(world, fakePlayer, InteractionHand.MAIN_HAND, stack, hit);
            stack.getItem().useOn(context);
        } else {
            state.use(world, fakePlayer, InteractionHand.OFF_HAND, hit);
        }
        return TickRateModulation.IDLE;
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        getHost().markForSave();
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(UPGRADES)) {
            return getUpgrades();
        }
        return super.getSubInventory(id);
    }
}