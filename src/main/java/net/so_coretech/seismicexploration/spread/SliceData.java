package net.so_coretech.seismicexploration.spread;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.MapColor;
import net.so_coretech.seismicexploration.screen.RecorderScreen;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

public class SliceData {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int SLICE_SIZE = 320 * 320;
    public int centerX;
    public int centerZ;
    public int axis = RecorderScreen.AXIS_X;
    public byte[] colors = new byte[SLICE_SIZE];

    public SliceData() {
    }

    public SliceData(final int centerX, final int centerZ, final int axis,
                     final ByteBuffer colors) {
        this(centerX, centerZ, axis, colors.array().length == SLICE_SIZE ? colors.array() : new byte[SLICE_SIZE]);
    }

    public SliceData(final int centerX, final int centerZ, final int axis,
                     final byte[] colors) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.axis = axis;
        this.colors = colors;
    }

    public static SliceData fromBlocks(final int centerX, final int centerZ, final int axis,
                                       final Map<BlockPos, Byte> blocks) {
        LOGGER.debug("Creating slice data for {}, {}, {}", centerX, centerZ, axis);
        final byte[] colors = new byte[SliceData.SLICE_SIZE];
        final int left;
        if (axis == RecorderScreen.AXIS_X) {
            left = centerX - 160;
        } else {
            left = centerZ - 160;
        }
        for (int i = 0; i < 320; i++) {
            for (int y = 255; y >= -64; y--) {
                Byte color;
                if (axis == RecorderScreen.AXIS_X) {
                    color = blocks.get(new BlockPos(left + i, y, centerZ));
                } else {
                    color = blocks.get(new BlockPos(centerX, y, left + i));
                }
                if (color == null) {
                    color = MapColor.NONE.getPackedId(MapColor.Brightness.HIGH);
                }
                final int k = i * 320 + (255 - y);
                colors[k] = color;
            }
        }
        return new SliceData(centerX, centerZ, axis, ByteBuffer.wrap(colors));
    }

    public Tag serializeNBT() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("centerX", centerX);
        tag.putInt("centerZ", centerZ);
        tag.putInt("axis", axis);
        tag.putByteArray("colors", colors);
        return tag;
    }

    public static SliceData fromNBT(final CompoundTag tag) {
        final int centerX = tag.getIntOr("centerX", 0);
        final int centerZ = tag.getIntOr("centerZ", 0);
        final int axis = tag.getIntOr("axis", RecorderScreen.AXIS_X);
        final byte[] colors;
        final Optional<byte[]> colorsTag = tag.getByteArray("colors");
        // Ensure colors array is the right size
        if (colorsTag.isEmpty() || colorsTag.get().length != SLICE_SIZE) {
            colors = new byte[SLICE_SIZE];
        } else {
            colors = colorsTag.get();
        }
        return new SliceData(centerX, centerZ, axis, colors);
    }

    @Override
    public String toString() {
        return String.format("centerX: %d, centerZ: %d, axis: %s", centerX, centerZ, axis);
    }
}
