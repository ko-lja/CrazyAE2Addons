package net.oktawia.crazyae2addons.mobstorage;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.oktawia.crazyae2addons.CrazyAddons;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.stream.Stream;

public class MobKeyType extends AEKeyType {

    public static final AEKeyType TYPE = new MobKeyType();

    private MobKeyType() {
        super(CrazyAddons.makeId("mob"), MobKey.class, Component.literal("mobs"));
    }

    @Nullable
    @Override
    public AEKey readFromPacket(FriendlyByteBuf input) {
        DataResult<MobKey> result = MobKey.CODEC.parse(NbtOps.INSTANCE, input.readNbt());
        Optional<MobKey> dataOptional = result.result();
        return dataOptional.get();
    }

    @Nullable
    @Override
    public AEKey loadKeyFromTag(CompoundTag tag) {
        DataResult<MobKey> result = MobKey.CODEC.parse(NbtOps.INSTANCE, tag);
        Optional<MobKey> dataOptional = result.result();
        return dataOptional.get();
    }

    @Override
    public Stream<TagKey<?>> getTagNames() {
        return Stream.empty();
    }

    @Override
    public String getUnitSymbol() {
        return "Mobs";
    }

    public static void registerContainerItemStrategies(){
        ContainerItemStrategies.register(
            MobKeyType.TYPE,
            MobKey.class,
                new ContainerItemStrategy() {
                    @Override
                    public @Nullable GenericStack getContainedStack(ItemStack stack) {
                        if (!(stack.getItem() instanceof SpawnEggItem egg)) return null;

                        EntityType<?> type = egg.getType(stack.getTag());
                        if (type == null) return null;

                        MobKey key = MobKey.of(type);
                        return new GenericStack(key, 1L);
                    }
                    @Override public @Nullable MobKey findCarriedContext(Player player, AbstractContainerMenu menu) { return null; }
                    @Override public @Nullable GenericStack getExtractableContent(Object context) { return null; }
                    @Override public long insert(Object ctx, AEKey what, long amount, Actionable mode)   { return 0; }
                    @Override public long extract(Object ctx, AEKey what, long amount, Actionable mode)  { return 0; }
                    @Override public void playFillSound(Player player, AEKey what)  {}
                    @Override public void playEmptySound(Player player, AEKey what) {}
            }
        );
    }
}
