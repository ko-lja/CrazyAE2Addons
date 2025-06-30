package net.oktawia.crazyae2addons.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CradleEntry(ResourceLocation structureId, List<ItemStack> inputs, ItemStack output) {}
