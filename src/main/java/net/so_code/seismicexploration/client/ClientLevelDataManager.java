package net.so_code.seismicexploration.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.so_code.seismicexploration.spread.SliceSavedData;

public class ClientLevelDataManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static ClientLevelDataManager instance;

    private Map<BlockPos, Byte> blocks = new HashMap<>();
    private final Map<String, SliceSavedData> cache = new HashMap<>();
    private Optional<Integer> centerX = Optional.empty();
    private Optional<Integer> centerZ = Optional.empty();
    private Optional<Axis> axis = Optional.empty();

    private ClientLevelDataManager() {}

    public static ClientLevelDataManager get() {
        if (instance == null) {
            instance = new ClientLevelDataManager();
        }
        return instance;
    }

    public void setBlocks(final Map<BlockPos, Byte> blocks) {
        this.blocks = blocks;
        cache.clear();
    }

    public Optional<Integer> getCenterX() {
        return centerX;
    }

    public Optional<Integer> getCenterZ() {
        return centerZ;
    }

    public Optional<Axis> getAxis() {
        return axis;
    }

    public void setRecorderParameters(final int centerX, final int centerZ, final Axis axis) {
        this.centerX = Optional.of(centerX);
        this.centerZ = Optional.of(centerZ);
        this.axis = Optional.of(axis);
    }

    public SliceSavedData getSliceSavedData(final int centerX, final int centerZ, final Axis axis) {
        SliceSavedData data =
                cache.get(String.format("%d-%d-%s", centerX, centerZ, axis.getName()));
        if (data == null) {
            LOGGER.debug("Creating SliceSavedData for {}, {}, {}", centerX, centerZ, axis);
            final byte[] colors = new byte[SliceSavedData.SLICE_SIZE];
            final int left;
            if (axis == Axis.X) {
                left = centerX - 160;
            } else {
                left = centerZ - 160;
            }
            for (int i = 0; i < 320; i++) {
                for (int y = 255; y >= -64; y--) {
                    Byte color;
                    if (axis == Axis.X) {
                        color = blocks.get(new BlockPos(left + i, y, centerZ));
                    } else {
                        color = blocks.get(new BlockPos(centerX, y, left + i));
                    }
                    if (color == null) {
                        color = MapColor.NONE.getPackedId(Brightness.HIGH);
                    }
                    final int k = i * 320 + (255 - y);
                    colors[k] = color;
                }
            }
            data = new SliceSavedData(centerX, centerZ, axis, ByteBuffer.wrap(colors));
            cache.put(String.format("%d-%d-%s", centerX, centerZ, axis.getName()), data);
        }
        return data;
    }
}
