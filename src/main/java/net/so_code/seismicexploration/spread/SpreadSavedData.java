package net.so_code.seismicexploration.spread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SpreadSavedData extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SavedDataType<SpreadSavedData> TYPE = new SavedDataType<SpreadSavedData>(
            "spread", SpreadSavedData::new, BlockPos.CODEC.listOf().xmap(list -> {
                SpreadSavedData data = new SpreadSavedData();
                data.placedSensors.addAll(list);
                return data;
            }, data -> new ArrayList<>(data.placedSensors)), DataFixTypes.LEVEL);

    private Set<BlockPos> placedSensors = new HashSet<>();

    public static SpreadSavedData get(DimensionDataStorage storage) {
        SpreadSavedData data = storage.computeIfAbsent(TYPE);
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
