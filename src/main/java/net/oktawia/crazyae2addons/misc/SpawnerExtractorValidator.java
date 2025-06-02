package net.oktawia.crazyae2addons.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.oktawia.crazyae2addons.blocks.SpawnerExtractorControllerBlock;
import net.oktawia.crazyae2addons.blocks.SpawnerExtractorWallBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerExtractorValidator {

    private static final String STRUCTURE_JSON = """
    {
      "symbols": {
        "A": ["minecraft:air"],
        "G": ["ae2:quartz_vibrant_glass"],
        "W": ["crazyae2addons:spawner_extractor_wall"],
        "C": ["crazyae2addons:spawner_extractor_controller"]
      },
      "layers": [
        [
          "W W W W W",
          "W W W W W",
          "W W W W W",
          "W W W W W",
          "W W C W W"
        ],
        [
          "W G G G W",
          "G A A A G",
          "G A A A G",
          "G A A A G",
          "W G G G W"
        ],
        [
          "W G G G W",
          "G A A A G",
          "G A . A G",
          "G A A A G",
          "W G G G W"
        ],
        [
          "W G G G W",
          "G A A A G",
          "G A A A G",
          "G A A A G",
          "W G G G W"
        ],
        [
          "W W W W W",
          "W W W W W",
          "W W W W W",
          "W W W W W",
          "W W W W W"
        ]
      ]
    }
    """;

    private final Map<String, List<Block>> symbols = new HashMap<>();
    private final List<List<String>> layers = new ArrayList<>();
    private int originInPatternX = -1;
    private int originInPatternY = -1;
    private int originInPatternZ = -1;

    public SpawnerExtractorValidator() {
        JsonObject json = JsonParser.parseString(STRUCTURE_JSON).getAsJsonObject();

        JsonObject symbolsJson = json.getAsJsonObject("symbols");
        for (Map.Entry<String, JsonElement> entry : symbolsJson.entrySet()) {
            List<Block> blocks = new ArrayList<>();
            for (JsonElement el : entry.getValue().getAsJsonArray()) {
                ResourceLocation id = new ResourceLocation(el.getAsString());
                Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
                if (block != null) {
                    blocks.add(block);
                }
            }
            symbols.put(entry.getKey(), blocks);
        }

        JsonArray layersJson = json.getAsJsonArray("layers");
        for (int y = 0; y < layersJson.size(); y++) {
            JsonArray layerJson = layersJson.get(y).getAsJsonArray();
            List<String> layer = new ArrayList<>();
            for (int z = 0; z < layerJson.size(); z++) {
                String row = layerJson.get(z).getAsString();
                String[] parts = row.split(" ");
                for (int x = 0; x < parts.length; x++) {
                    if (parts[x].equals("C")) {
                        originInPatternX = x;
                        originInPatternY = y;
                        originInPatternZ = z;
                    }
                }
                layer.add(row);
            }
            layers.add(layer);
        }

        if (originInPatternX == -1 || originInPatternY == -1 || originInPatternZ == -1) {
            throw new IllegalStateException("Pattern does not contain origin symbol 'C'");
        }
    }

    public boolean matchesStructure(Level level, BlockPos origin, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        int height = layers.size();
        int sizeZ = layers.get(0).size();
        int sizeX = layers.get(0).get(0).split(" ").length;

        for (int y = 0; y < height; y++) {
            List<String> layer = layers.get(y);
            for (int z = 0; z < sizeZ; z++) {
                String[] row = layer.get(z).split(" ");
                for (int x = 0; x < sizeX; x++) {
                    String symbol = row[x];
                    if (symbol.equals(".")) continue;
                    int relX = x - originInPatternX;
                    int relZ = z - originInPatternZ;
                    int relY = y - originInPatternY;
                    BlockPos offset = rotateOffset(relX, relZ, facing);
                    BlockPos checkPos = origin.offset(offset.getX(), relY, offset.getZ());
                    BlockState checkState = level.getBlockState(checkPos);
                    Block block = checkState.getBlock();
                    List<Block> allowed = symbols.get(symbol);
                    if (allowed == null) {
                        return false;
                    }
                    boolean match = allowed.contains(block);
                    if (!match) {
                        markWalls(level, origin, state, SpawnerExtractorWallBlock.FORMED, false);
                        return false;
                    }
                }
            }
        }
        markWalls(level, origin, state, SpawnerExtractorWallBlock.FORMED, true);
        return true;
    }

    private BlockPos rotateOffset(int x, int z, Direction facing) {
        return switch (facing) {
            case NORTH -> new BlockPos(x, 0, z);
            case SOUTH -> new BlockPos(-x, 0, -z);
            case WEST  -> new BlockPos(z, 0, -x);
            case EAST  -> new BlockPos(-z, 0, x);
            default -> BlockPos.ZERO;
        };
    }

    public void markWalls(Level level, BlockPos origin, BlockState state, BooleanProperty formedProperty, boolean setState) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        int height = layers.size();
        int sizeZ = layers.get(0).size();
        int sizeX = layers.get(0).get(0).split(" ").length;

        for (int y = 0; y < height; y++) {
            List<String> layer = layers.get(y);
            for (int z = 0; z < sizeZ; z++) {
                String[] row = layer.get(z).split(" ");
                for (int x = 0; x < sizeX; x++) {
                    String symbol = row[x];
                    if (!symbol.equals("W")) continue;

                    int relX = x - originInPatternX;
                    int relZ = z - originInPatternZ;
                    int relY = y - originInPatternY;

                    BlockPos offset = rotateOffset(relX, relZ, facing);
                    BlockPos checkPos = origin.offset(offset.getX(), relY, offset.getZ());
                    BlockState blockState = level.getBlockState(checkPos);

                    if (blockState.hasProperty(formedProperty) && blockState.getValue(formedProperty) != setState) {
                        level.setBlock(checkPos, blockState.setValue(formedProperty, setState), 3);
                    }
                }
            }
        }
    }
}