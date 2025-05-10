package net.so_code.seismicexploration.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.so_code.seismicexploration.spread.SliceSavedData;

public class ClientLevelDataManager {

    private static ClientLevelDataManager instance;
    private final Map<BlockPos, Byte> blocks = new HashMap<>();

    private ClientLevelDataManager() {}

    public static ClientLevelDataManager get() {
        if (instance == null) {
            instance = new ClientLevelDataManager();
        }
        return instance;
    }

    public void putBlocks(final Map<BlockPos, Byte> blocks) {

    }

    public SliceSavedData getSliceSavedData(final int centerX, final int centerZ, final Axis axis) {
        final byte[] colors = new byte[SliceSavedData.SLICE_SIZE];
        // TODO: use axis
        final int leftX = centerX - 160;
        for (int x = 0; x < 320; x++) {
            for (int y = 255; y >= -64; y--) {
                Byte color = blocks.get(new BlockPos(leftX + x, y, centerZ));
                if (color == null) {
                    color = MapColor.NONE.getPackedId(Brightness.HIGH);
                }
                final int k = x * 320 + (255 - y);
                colors[k] = color;
            }
        }
        return new SliceSavedData(centerX, centerZ, axis, ByteBuffer.wrap(colors));
    }
}
