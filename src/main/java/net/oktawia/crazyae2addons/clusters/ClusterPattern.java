package net.oktawia.crazyae2addons.clusters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStreamReader;
import java.util.*;

public class ClusterPattern {
    private final List<List<String>> layers;
    private final Map<Character, Set<Block>> symbolMap;
    private final ResourceLocation patternLocation;

    public enum Rotation {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270
    }

    public ClusterPattern(ResourceLocation location) {
        this.patternLocation = location;
        ClusterPattern loaded = loadFromJson(location);
        this.layers = loaded.layers;
        this.symbolMap = loaded.symbolMap;
    }

    private ClusterPattern(List<List<String>> layers, Map<Character, Set<Block>> symbolMap, ResourceLocation location) {
        this.layers = layers;
        this.symbolMap = symbolMap;
        this.patternLocation = location;
    }

    public int[] rotateXZ(int x, int z, Rotation rotation) {
        int w = getWidth();
        int d = getDepth();

        return switch (rotation) {
            case NONE -> new int[]{x, z};
            case CLOCKWISE_90 -> new int[]{d - 1 - z, x};
            case CLOCKWISE_180 -> new int[]{w - 1 - x, d - 1 - z};
            case CLOCKWISE_270 -> new int[]{z, w - 1 - x};
        };
    }

    public static BlockPos findOrigin(Level level, BlockPos startPos, Set<Block> validBlocks) {
        BlockPos pos = startPos;
        while (validBlocks.contains(level.getBlockState(pos.below()).getBlock())) {
            pos = pos.below();
        }
        boolean moved;
        do {
            moved = false;
            for (Direction dir : List.of(Direction.NORTH, Direction.WEST)) {
                BlockPos next = pos.relative(dir);
                if (validBlocks.contains(level.getBlockState(next).getBlock())) {
                    pos = next;
                    moved = true;
                    break;
                }
            }
        } while (moved);
        return pos;
    }

    public boolean matchesWithRotation(Level level, BlockPos origin, Rotation rotation) {
        for (int y = 0; y < layers.size(); y++) {
            List<String> layer = layers.get(y);
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                for (int x = 0; x < row.length(); x++) {
                    char symbol = row.charAt(x);
                    if (symbol == '.') continue;
                    int[] rotated = rotateXZ(x, z, rotation);
                    BlockPos checkPos = origin.offset(rotated[0], y, rotated[1]);
                    Block actualBlock = level.getBlockState(checkPos).getBlock();
                    Set<Block> expected = symbolMap.get(symbol);
                    if (expected == null || !expected.contains(actualBlock)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public int countBlocks(Level level, BlockPos origin, Block targetBlock, Rotation rotation) {
        int count = 0;
        for (int y = 0; y < layers.size(); y++) {
            List<String> layer = layers.get(y);
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                for (int x = 0; x < row.length(); x++) {
                    char symbol = row.charAt(x);
                    if (symbol == '.') continue;
                    int[] rotated = rotateXZ(x, z, rotation);
                    BlockPos checkPos = origin.offset(rotated[0], y, rotated[1]);
                    if (level.getBlockState(checkPos).getBlock() == targetBlock) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int countBlocks(Level level, BlockPos startPos, Block targetBlock) {
        BlockPos origin = findOrigin(level, startPos, getAllValidBlocks());
        for (Rotation rot : Rotation.values()) {
            if (matchesWithRotation(level, origin, rot)) {
                return countBlocks(level, origin, targetBlock, rot);
            }
        }
        return 0;
    }

    public int getWidth() {
        return layers.get(0).get(0).length();
    }

    public int getDepth() {
        return layers.get(0).size();
    }

    public int getHeight() {
        return layers.size();
    }

    public Set<Block> getAllValidBlocks() {
        Set<Block> all = new HashSet<>();
        symbolMap.values().forEach(all::addAll);
        return all;
    }

    private static ClusterPattern loadFromJson(ResourceLocation location) {
        try {
            ResourceManager rm = Minecraft.getInstance().getResourceManager();
            Resource res = rm.getResource(new ResourceLocation(location.getNamespace(), "multis/" + location.getPath() + ".json")).orElseThrow(
                    () -> new IllegalStateException("Could not find cluster pattern: " + location)
            );
            InputStreamReader reader = new InputStreamReader(res.open());
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            reader.close();

            JsonObject symbols = json.getAsJsonObject("symbols");
            Map<Character, Set<Block>> map = new HashMap<>();
            for (String key : symbols.keySet()) {
                JsonArray arr = symbols.getAsJsonArray(key);
                Set<Block> blocks = new HashSet<>();
                for (var e : arr) {
                    Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e.getAsString()));
                    if (b == null) throw new IllegalStateException("Block not found: " + e.getAsString());
                    blocks.add(b);
                }
                map.put(key.charAt(0), blocks);
            }

            JsonArray layersJson = json.getAsJsonArray("layers");
            List<List<String>> layers = new ArrayList<>();
            for (var layerElem : layersJson) {
                JsonArray rowsJson = layerElem.getAsJsonArray();
                List<String> rows = new ArrayList<>();
                for (var rowElem : rowsJson) {
                    rows.add(rowElem.getAsString().replace(" ", ""));
                }
                layers.add(rows);
            }

            return new ClusterPattern(layers, map, location);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load ClusterPattern from " + location, e);
        }
    }
}
