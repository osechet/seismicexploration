package net.so_code.seismicexploration.spread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.so_code.seismicexploration.blockentity.SensorBlockEntity;

public class Spread extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SavedDataType<Spread> TYPE =
            new SavedDataType<Spread>("spread", Spread::new, BlockPos.CODEC.listOf().xmap(list -> {
                final Spread data = new Spread();
                data.placedSensors.addAll(list);
                return data;
            }, data -> new ArrayList<>(data.placedSensors)), DataFixTypes.LEVEL);

    private final Set<BlockPos> placedSensors = new HashSet<>();

    public static Spread getSpread(final ServerLevel level) {
        final DimensionDataStorage storage = level.getDataStorage();
        final Spread data = storage.computeIfAbsent(TYPE);
        LOGGER.debug("Spread loaded with {} positions", data.placedSensors.size());
        return data;
    }

    public void add(final BlockPos pos) {
        placedSensors.add(pos);
        this.setDirty();
    }

    public void remove(final BlockPos pos) {
        placedSensors.remove(pos);
        this.setDirty();
    }

    public Set<BlockPos> getPlacedSensors() {
        return Collections.unmodifiableSet(placedSensors);
    }

    @Nonnull
    public Slice getSlice(final Level level, final int x, final int z, final Axis axis) {
        // TODO: we should use getPackedId instead of calculateARGBColor
        final Map<BlockPos, Integer> blocks = placedSensors.stream() //
                .map(pos -> (SensorBlockEntity) level.getBlockEntity(pos)) //
                .map(sensor -> sensor.getBlocks()) //
                .flatMap(map -> map.entrySet().stream()) //
                .collect(Collectors.toMap(Map.Entry::getKey, //
                        entry -> entry.getValue().calculateARGBColor(Brightness.NORMAL), //
                        (existing, replacement) -> existing // keep the first value found
                ));

        return new Slice(level, x, z, axis, blocks);
    }

    private static final int BLOCKS_COUNT_ON_AXIS = 100;

    public static class Slice {

        public final int centerX;
        public final int centerZ;
        public int maxY;
        public int[] colors;

        public Slice(@Nonnull final Level level, final int centerX, final int centerZ,
                final Axis axis, final Map<BlockPos, Integer> blocks) {
            this.centerX = centerX;
            this.centerZ = centerZ;

            final int minY = level.getMinY();
            this.maxY = minY;
            final List<Integer> colors;
            switch (axis) {
                case X:
                    // First find the max y
                    for (int x = centerX - (BLOCKS_COUNT_ON_AXIS / 2); x <= centerX
                            + (BLOCKS_COUNT_ON_AXIS / 2); x++) {
                        final int columnMaxY =
                                level.getHeight(Heightmap.Types.WORLD_SURFACE, x, centerZ);
                        if (columnMaxY > this.maxY) {
                            this.maxY = columnMaxY;
                        }
                    }
                    // Create all the cells
                    colors = new ArrayList<>(BLOCKS_COUNT_ON_AXIS * this.maxY);
                    for (int x = centerX - (BLOCKS_COUNT_ON_AXIS / 2); x <= centerX
                            + (BLOCKS_COUNT_ON_AXIS / 2); x++) {
                        for (int y = minY; y < this.maxY; y++) {
                            colors.add(blocks.get(new BlockPos(x, y, centerZ)));
                        }
                    }
                    break;
                case Z:
                    // First find the max y
                    for (int z = centerZ - (BLOCKS_COUNT_ON_AXIS / 2); z <= centerZ
                            + (BLOCKS_COUNT_ON_AXIS / 2); z++) {
                        final int columnMaxY =
                                level.getHeight(Heightmap.Types.WORLD_SURFACE, centerX, z);
                        if (columnMaxY > this.maxY) {
                            this.maxY = columnMaxY;
                        }
                    }
                    // Create all the cells
                    colors = new ArrayList<>(BLOCKS_COUNT_ON_AXIS * this.maxY);
                    for (int z = centerZ - (BLOCKS_COUNT_ON_AXIS / 2); z <= centerZ
                            + (BLOCKS_COUNT_ON_AXIS / 2); z++) {
                        for (int y = minY; y < this.maxY; y++) {
                            colors.add(blocks.get(new BlockPos(centerX, y, z)));
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("invalid axis");
            }

            this.colors = colors.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}
