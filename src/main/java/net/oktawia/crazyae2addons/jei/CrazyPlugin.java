package net.oktawia.crazyae2addons.jei;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;

import java.util.List;

@JeiPlugin
public class CrazyPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("crazyae2addons", "jei_plugin");
    public static CrazyEntry currentEntry;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CrazyCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ReinforcedCondenserCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CradleCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CrazyEntry> entries = List.of(
                new CrazyEntry(
                        new ResourceLocation("crazyae2addons", "penrose_sphere.nbt"),
                        Component.literal("Penrose Sphere"),
                        List.of(
                                new ItemStack(CrazyBlockRegistrar.PENROSE_FRAME.get()).copyWithCount(1298),
                                new ItemStack(CrazyBlockRegistrar.PENROSE_COIL.get()).copyWithCount(307),
                                new ItemStack(CrazyBlockRegistrar.PENROSE_PORT.get()).copyWithCount(4),
                                new ItemStack(CrazyBlockRegistrar.PENROSE_CONTROLLER.get()).copyWithCount(1)
                        )
                ),

                new CrazyEntry(
                        new ResourceLocation("crazyae2addons", "energy_storage.nbt"),
                        Component.literal("Energy Storage"),
                        List.of(
                                new ItemStack(CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_256K_BLOCK.get()).copyWithCount(279),
                                new ItemStack(AEBlocks.QUARTZ_VIBRANT_GLASS).copyWithCount(216),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_FRAME_BLOCK.get()).copyWithCount(202),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_PORT_BLOCK.get()).copyWithCount(3),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_CONTROLLER_BLOCK.get()).copyWithCount(1)
                        )
                ),

                new CrazyEntry(
                        new ResourceLocation("crazyae2addons", "entropy_cradle.nbt"),
                        Component.literal("Entropy Cradle"),
                        List.of(
                                new ItemStack(CrazyBlockRegistrar.ENTROPY_CRADLE.get()).copyWithCount(236),
                                new ItemStack(CrazyBlockRegistrar.ENTROPY_CRADLE_CAPACITOR.get()).copyWithCount(24),
                                new ItemStack(CrazyBlockRegistrar.ENTROPY_CRADLE_CONTROLLER.get()).copyWithCount(1)
                        )
                ),

                new CrazyEntry(
                        new ResourceLocation("crazyae2addons", "spawner_extractor.nbt"),
                        Component.literal("Spawner Extractor"),
                        List.of(
                                new ItemStack(CrazyBlockRegistrar.SPAWNER_EXTRACTOR_WALL.get()).copyWithCount(101),
                                new ItemStack(AEBlocks.QUARTZ_VIBRANT_GLASS).copyWithCount(36),
                                new ItemStack(CrazyBlockRegistrar.SPAWNER_EXTRACTOR_CONTROLLER.get()).copyWithCount(1)
                        )
                ),

                new CrazyEntry(
                        new ResourceLocation("crazyae2addons", "mob_farm.nbt"),
                        Component.literal("Mob Farm"),
                        List.of(
                                new ItemStack(CrazyBlockRegistrar.MOB_FARM_WALL.get()).copyWithCount(113),
                                new ItemStack(CrazyBlockRegistrar.MOB_FARM_COLLECTOR.get()).copyWithCount(18),
                                new ItemStack(CrazyBlockRegistrar.MOB_FARM_DAMAGE.get()).copyWithCount(16),
                                new ItemStack(CrazyBlockRegistrar.MOB_FARM_INPUT.get()).copyWithCount(2),
                                new ItemStack(CrazyBlockRegistrar.MOB_FARM_CONTROLLER.get()).copyWithCount(1)
                        )
                ),

                new CrazyEntry(
                        new ResourceLocation("crazyae2addons", "pattern_unit.nbt"),
                        Component.literal("Pattern Management Unit"),
                        List.of(
                                new ItemStack(CrazyBlockRegistrar.PATTERN_MANAGEMENT_UNIT_WALL_BLOCK.get()).copyWithCount(54),
                                new ItemStack(CrazyBlockRegistrar.PATTERN_MANAGEMENT_UNIT_FRAME_BLOCK.get()).copyWithCount(44),
                                new ItemStack(CrazyBlockRegistrar.PATTERN_MANAGEMENT_UNIT_BLOCK.get()).copyWithCount(27),
                                new ItemStack(CrazyBlockRegistrar.PATTERN_MANAGEMENT_UNIT_CONTROLLER_BLOCK.get()).copyWithCount(1)
                        )
                )
        );

        List<CrazyWrapper> wrapped = entries.stream().map(CrazyWrapper::new).toList();
        registration.addRecipes(CrazyCategory.TYPE, wrapped);

        registration.addRecipes(ReinforcedCondenserCategory.TYPE, List.of(
                new ReinforcedCondenserEntry(
                        new ItemStack(AEItems.SINGULARITY).copyWithCount(8192),
                        new ItemStack(CrazyItemRegistrar.SUPER_SINGULARITY.get())
                )
        ));

        List<CradleEntry> raw = List.of(
                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "1k_storage.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(AEBlocks.CRAFTING_STORAGE_1K).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_1K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "4k_storage.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(AEBlocks.CRAFTING_STORAGE_4K).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_4K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "16k_storage.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(AEBlocks.CRAFTING_STORAGE_16K).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_16K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "64k_storage.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(AEBlocks.CRAFTING_STORAGE_64K).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_64K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "256k_storage.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(AEBlocks.CRAFTING_STORAGE_256K).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_256K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "1k_storage_dense.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.DENSE_ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_1K_BLOCK.get()).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_1K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "4k_storage_dense.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.DENSE_ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_4K_BLOCK.get()).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_4K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "16k_storage_dense.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.DENSE_ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_16K_BLOCK.get()).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_16K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "64k_storage_dense.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.DENSE_ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_64K_BLOCK.get()).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_64K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "256k_storage_dense.nbt"),
                        List.of(
                                new ItemStack(AEBlocks.DENSE_ENERGY_CELL).copyWithCount(56),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(30),
                                new ItemStack(CrazyBlockRegistrar.ENERGY_STORAGE_256K_BLOCK.get()).copyWithCount(27),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(12)
                        ),
                        new ItemStack(CrazyBlockRegistrar.DENSE_ENERGY_STORAGE_256K_BLOCK.get())
                ),

                new CradleEntry(
                        new ResourceLocation("crazyae2addons", "penrose_frame.nbt"),
                        List.of(
                                new ItemStack(Blocks.OBSIDIAN).copyWithCount(54),
                                new ItemStack(AEBlocks.FLUIX_BLOCK).copyWithCount(44),
                                new ItemStack(Blocks.IRON_BLOCK).copyWithCount(26),
                                new ItemStack(CrazyBlockRegistrar.SUPER_SINGULARITY_BLOCK.get()).copyWithCount(1)
                        ),
                        new ItemStack(CrazyBlockRegistrar.PENROSE_FRAME.get())
                )
        );

        List<CradleWrapper> wrapped2 = raw.stream()
                .map(CradleWrapper::new)
                .toList();

        registration.addRecipes(CradleCategory.TYPE, wrapped2);
    }
}
