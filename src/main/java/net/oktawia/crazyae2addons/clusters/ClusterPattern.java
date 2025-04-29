package net.oktawia.crazyae2addons.clusters;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ClusterPattern {
    private final List<List<String>> layers;
    private final Map<Character, Set<Block>> symbolMap;

    public enum Rotation {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270
    }

    public ClusterPattern(String pattern, Map<Character, Set<Block>> symbolMap) {
        this.symbolMap = symbolMap;
        this.layers = Arrays.stream(pattern.split(Pattern.quote("|")))
                .map(layer -> Arrays.asList(layer.split("/")))
                .toList();
    }

    public BlockPos findControllerOffset(Rotation rotation) {
        for (int y = 0; y < layers.size(); y++) {
            List<String> layer = layers.get(y);
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                for (int x = 0; x < row.length(); x++) {
                    if (row.charAt(x) == 'C') {
                        int[] rotated = rotateXZ(x, z, rotation);
                        return new BlockPos(rotated[0], y, rotated[1]);
                    }
                }
            }
        }
        return null;
    }

    public static int[] rotateXZ(int relX, int relZ, Rotation rotation) {
        return switch (rotation) {
            case NONE -> new int[]{relX, relZ};
            case CLOCKWISE_90 -> new int[]{-relZ, relX};
            case CLOCKWISE_180 -> new int[]{-relX, -relZ};
            case CLOCKWISE_270 -> new int[]{relZ, -relX};
        };
    }

    public boolean matchesWithRotation(Level level, BlockPos controllerPos, Rotation rotation) {
        BlockPos controllerOffset = findControllerOffset(rotation);
        if (controllerOffset == null) return false;
        BlockPos origin = controllerPos.subtract(controllerOffset);

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

                    Set<Block> expectedBlocks = symbolMap.get(symbol);
                    if (expectedBlocks == null || !expectedBlocks.contains(actualBlock)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public Map<Character, Set<Block>> getSymbolMap() {
        return symbolMap;
    }

    public int getWidth() {
        return layers.get(0).get(0).length();
    }

    public int getHeight() {
        return layers.size();
    }

    public List<List<String>> getLayers() {
        return layers;
    }
}
