package net.so_code.seismicexploration.spread;

import java.nio.ByteBuffer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SliceSavedData extends SavedData {

    public static final Codec<SliceSavedData> CODEC = RecordCodecBuilder.create(o -> o.group( //
            Codec.INT.fieldOf("centerX").forGetter(data -> data.centerX), //
            Codec.INT.fieldOf("centerZ").forGetter(data -> data.centerZ), //
            Codec.STRING.fieldOf("axis").xmap(Axis::valueOf, Axis::name)
                    .forGetter(data -> data.axis),
            Codec.BYTE_BUFFER.fieldOf("colors").forGetter(data -> ByteBuffer.wrap(data.colors)))
            .apply(o, SliceSavedData::new));

    public static final SavedDataType<SliceSavedData> TYPE = new SavedDataType<SliceSavedData>(
            "slice", SliceSavedData::new, CODEC, DataFixTypes.LEVEL);

    public static final int SLICE_SIZE = 320 * 320;
    public int centerX;
    public int centerZ;
    public Axis axis;
    public byte[] colors = new byte[SLICE_SIZE];

    public SliceSavedData() {}

    public SliceSavedData(final int centerX, final int centerZ, final Axis axis,
            final ByteBuffer colors) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.axis = axis;
        if (colors.array().length == SLICE_SIZE) {
            this.colors = colors.array();
        }
    }

    public static SliceSavedData getSlice(final ServerLevel level, final int center,
            final Axis axis) {
        final DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public void dummy() {
        for (int x = 0; x < 320; x++) {
            for (int y = 255; y >= -64; y--) {
                MapColor color;
                if (y > 67) {
                    color = MapColor.NONE;
                } else if (x == 159) {
                    color = MapColor.COLOR_BLUE;
                } else if (x == y) {
                    color = MapColor.EMERALD;
                } else if (y == -25) {
                    color = MapColor.FIRE;
                } else if (x == 72) {
                    color = MapColor.CLAY;
                } else {
                    color = MapColor.STONE;
                }
                final int k = x * 320 + (255 - y);
                colors[k] = color.getPackedId(Brightness.HIGH);
            }
        }
    }
}
