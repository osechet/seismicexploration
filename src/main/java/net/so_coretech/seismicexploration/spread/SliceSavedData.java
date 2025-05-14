package net.so_coretech.seismicexploration.spread;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class SliceSavedData extends SavedData {

    public static final Codec<SliceSavedData> CODEC = RecordCodecBuilder.create(o -> o.group( //
                                                                                          Codec.INT.fieldOf("centerX").forGetter(data -> data.centerX), //
                                                                                          Codec.INT.fieldOf("centerZ").forGetter(data -> data.centerZ), //
                                                                                          Codec.STRING.fieldOf("axis").xmap(Axis::valueOf, Axis::name)
                                                                                                      .forGetter(data -> data.axis),
                                                                                          Codec.BYTE_BUFFER.fieldOf("colors").forGetter(data -> ByteBuffer.wrap(data.colors)))
                                                                                      .apply(o, SliceSavedData::new));

    public static final SavedDataType<SliceSavedData> TYPE = new SavedDataType<>(
        "slice", SliceSavedData::new, CODEC, DataFixTypes.LEVEL);

    public static final int SLICE_SIZE = 320 * 320;
    public int centerX;
    public int centerZ;
    public @Nullable Axis axis;
    public byte[] colors = new byte[SLICE_SIZE];

    public SliceSavedData() {
    }

    public SliceSavedData(final int centerX, final int centerZ, final Axis axis,
                          final ByteBuffer colors) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.axis = axis;
        if (colors.array().length == SLICE_SIZE) {
            this.colors = colors.array();
        }
    }
}
