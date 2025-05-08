package net.so_code.seismicexploration.spread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class Spread extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SavedDataType<Spread> TYPE =
            new SavedDataType<Spread>("spread", Spread::new, BlockPos.CODEC.listOf().xmap(list -> {
                Spread data = new Spread();
                data.placedSensors.addAll(list);
                return data;
            }, data -> new ArrayList<>(data.placedSensors)), DataFixTypes.LEVEL);

    private Set<BlockPos> placedSensors = new HashSet<>();

    public static Spread getSpread(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return Spread.get(storage);
    }

    public static Spread get(DimensionDataStorage storage) {
        Spread data = storage.computeIfAbsent(TYPE);
        LOGGER.debug("Spread loaded with {} positions", data.placedSensors.size());
        return data;
    }

    public void add(BlockPos pos) {
        placedSensors.add(pos);
        this.setDirty();
    }

    public void remove(BlockPos pos) {
        placedSensors.remove(pos);
        this.setDirty();
    }

    public Set<BlockPos> getPlacedSensors() {
        return Collections.unmodifiableSet(placedSensors);
    }
}
