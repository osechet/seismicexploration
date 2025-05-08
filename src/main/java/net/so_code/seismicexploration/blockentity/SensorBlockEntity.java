package net.so_code.seismicexploration.blockentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.so_code.seismicexploration.ModBlockEntities;
import net.so_code.seismicexploration.SeismicExploration;

public class SensorBlockEntity extends BlockEntity implements TickableBlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private BlockPos recordingPos;
    private int blocksPerTick = 0;

    private Map<BlockPos, MapColor> blocks = new HashMap<>();

    public SensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SENSOR_ENTITY.get(), pos, state);
    }

    @SuppressWarnings("null")
    public void startRecording(@Nonnull BlockPos pos) {
        LOGGER.debug("Sensor at {} starting recording at {}", worldPosition, pos);
        recordingPos = pos;
        // Calculate how many blocks must be recorded per tick. We limit the number of blocks being
        // browsed per tick to avoid performance issues
        int maxY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        int blocksCount = maxY - pos.getY();
        int ticksToRecord = (BoomBoxBlockEntity.cyclesCount) * BoomBoxBlockEntity.ticksPerCycle;
        blocksPerTick = (int) Math.ceil((float) blocksCount / (float) ticksToRecord);

        LOGGER.trace("blocksCount = {} - ticksToRecord = {} - blocksPerTick = {}", blocksCount,
                ticksToRecord, blocksPerTick);
    }

    @SuppressWarnings("null")
    public void tick() {
        if (level != null) {
            final Level lvl = level;
            if (recordingPos != null) {
                // When recordingPos is set, it means the sensor is recording
                final int maxY = lvl.getHeight(Heightmap.Types.WORLD_SURFACE, recordingPos.getX(),
                        recordingPos.getZ());
                for (int i = 0; i < blocksPerTick; i++) {
                    if (recordingPos.getY() < maxY) {
                        LOGGER.debug("Sensor at {} recording block at {}", worldPosition,
                                recordingPos);
                        blocks.put(recordingPos,
                                Blocks.DIRT.defaultBlockState().getMapColor(level, recordingPos));
                        recordingPos = recordingPos.above();
                    }
                }
                setChanged();

                if (recordingPos.getY() >= maxY) {
                    recordingPos = null;
                    blocksPerTick = 0;
                }
            }
        }
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registry) {
        LOGGER.debug("loadAdditional");
        super.loadAdditional(tag, registry);

        CompoundTag compound = tag.getCompoundOrEmpty(SeismicExploration.MODID);

        // Load blocks map
        blocks.clear();
        if (compound.contains("blocks")) {
            ListTag blocksList = compound.getListOrEmpty("blocks");
            for (int i = 0; i < blocksList.size(); i++) {
                CompoundTag blockTag = blocksList.getCompoundOrEmpty(i);
                Optional<Integer> x = blockTag.getInt("x");
                Optional<Integer> y = blockTag.getInt("y");
                Optional<Integer> z = blockTag.getInt("z");
                Optional<Integer> colorId = blockTag.getInt("color");
                if (!x.isPresent() || !y.isPresent() || !z.isPresent() || !colorId.isPresent()) {
                    LOGGER.warn("Invalid block tag");
                    continue;
                }
                BlockPos pos = new BlockPos(x.get(), y.get(), z.get());
                MapColor color = MapColor.byId(colorId.get());
                blocks.put(pos, color);
            }
        }
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registry) {
        LOGGER.debug("saveAdditional");
        super.saveAdditional(tag, registry);

        CompoundTag compound = new CompoundTag();

        // Save blocks map
        ListTag blocksList = new ListTag();
        for (Map.Entry<BlockPos, MapColor> entry : blocks.entrySet()) {
            CompoundTag blockTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            blockTag.putInt("x", pos.getX());
            blockTag.putInt("y", pos.getY());
            blockTag.putInt("z", pos.getZ());
            blockTag.putInt("color", entry.getValue().id);
            blocksList.add(blockTag);
        }
        compound.put("blocks", blocksList);

        tag.put(SeismicExploration.MODID, compound);
    }
}
