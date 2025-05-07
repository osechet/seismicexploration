package net.so_code.seismicexploration.spread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SpreadSavedData extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Codec<SpreadSavedData> CODEC = BlockPos.CODEC.listOf().xmap(list -> {
        SpreadSavedData data = new SpreadSavedData();
        data.placedSensors.addAll(list);
        return data;
    }, data -> new ArrayList<>(data.placedSensors));

    private Set<BlockPos> placedSensors = new HashSet<>();

    public static SpreadSavedData get(DimensionDataStorage storage) {
        SpreadSavedData temp = storage.computeIfAbsent(new SavedDataType<SpreadSavedData>("spread",
                () -> new SpreadSavedData(), CODEC, DataFixTypes.LEVEL));
        LOGGER.debug("Spread loaded with {} positions", temp.placedSensors.size());
        return temp;
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
