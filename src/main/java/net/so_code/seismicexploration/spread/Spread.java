package net.so_code.seismicexploration.spread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    public Object getSlice(final Level level, final int x, final int z, final Axis axis) {
        // TODO: we should use getPackedId instead of calculateARGBColor
        final Map<BlockPos, Integer> blocks = placedSensors.stream() //
                .map(pos -> (SensorBlockEntity) level.getBlockEntity(pos)) //
                .map(sensor -> sensor.getBlocks()) //
                .flatMap(map -> map.entrySet().stream()) //
                .collect(Collectors.toMap(Map.Entry::getKey, //
                        entry -> entry.getValue().calculateARGBColor(Brightness.NORMAL), //
                        (existing, replacement) -> existing // keep the first value found
                ));

        return null;
    }
}
