package net.so_coretech.seismicexploration.blockentity;

import com.mojang.logging.LogUtils;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import org.slf4j.Logger;

public class SensorBlockEntity extends BlockEntity implements TickableBlockEntity {

  private static final Logger LOGGER = LogUtils.getLogger();

  private @Nullable BlockPos recordingPos;
  private int blocksPerTick = 0;

  private final Map<BlockPos, MapColor> blocks = new HashMap<>();

  public SensorBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.SENSOR_ENTITY.get(), pos, state);
  }

  public Map<BlockPos, MapColor> getBlocks() {
    return Collections.unmodifiableMap(blocks);
  }

  public void startRecording(final BlockPos pos) {
    LOGGER.debug("Sensor at {} starting recording at {}", worldPosition, pos);
    recordingPos = pos;
    if (level != null) {
      // Calculate how many blocks must be recorded per tick. We limit the number of blocks being
      // browsed per tick to avoid performance issues
      final int maxY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
      final int blocksCount = maxY - pos.getY();
      final int ticksToRecord = (BoomBoxBlockEntity.cyclesCount) * BoomBoxBlockEntity.ticksPerCycle;
      blocksPerTick = (int) Math.ceil((float) blocksCount / (float) ticksToRecord);

      LOGGER.trace(
          "blocksCount = {} - ticksToRecord = {} - blocksPerTick = {}",
          blocksCount,
          ticksToRecord,
          blocksPerTick);
    }
  }

  @Override
  public void tick() {
    if (level != null) {
      final Level lvl = level;
      if (recordingPos != null) {
        // When recordingPos is set, it means the sensor is recording
        final int maxY =
            lvl.getHeight(Heightmap.Types.WORLD_SURFACE, recordingPos.getX(), recordingPos.getZ());
        for (int i = 0; i < blocksPerTick; i++) {
          if (recordingPos.getY() < maxY) {
            LOGGER.trace("Sensor at {} recording block at {}", worldPosition, recordingPos);
            blocks.put(
                recordingPos, level.getBlockState(recordingPos).getMapColor(level, recordingPos));
            recordingPos = recordingPos.above();
          }
        }

        if (recordingPos.getY() >= maxY) {
          recordingPos = null;
          blocksPerTick = 0;
        }

        setChanged();
      }
    }
  }

  @Override
  protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
    LOGGER.trace(
        "loadAdditional - {}",
        level == null ? "server" : level.isClientSide() ? "client" : "server");
    super.loadAdditional(tag, registry);

    final CompoundTag compound = tag.getCompoundOrEmpty(SeismicExploration.MODID);

    // Load blocks map
    blocks.clear();
    if (compound.contains("blocks")) {
      final ListTag blocksList = compound.getListOrEmpty("blocks");
      for (int i = 0; i < blocksList.size(); i++) {
        final CompoundTag blockTag = blocksList.getCompoundOrEmpty(i);
        final Optional<Integer> x = blockTag.getInt("x");
        final Optional<Integer> y = blockTag.getInt("y");
        final Optional<Integer> z = blockTag.getInt("z");
        final Optional<Integer> colorId = blockTag.getInt("color");
        if (x.isEmpty() || y.isEmpty() || z.isEmpty() || colorId.isEmpty()) {
          LOGGER.warn("Invalid block tag");
          continue;
        }
        final BlockPos pos = new BlockPos(x.get(), y.get(), z.get());
        final MapColor color = MapColor.byId(colorId.get());
        blocks.put(pos, color);
      }
    }

    LOGGER.trace("loadAdditional - {} blocks", blocks.size());
  }

  @Override
  protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
    LOGGER.trace(
        "saveAdditional - {}", Objects.requireNonNull(level).isClientSide() ? "client" : "server");
    super.saveAdditional(tag, registry);

    final CompoundTag compound = new CompoundTag();

    // Save blocks map
    final ListTag blocksList = new ListTag();
    for (final Map.Entry<BlockPos, MapColor> entry : blocks.entrySet()) {
      final CompoundTag blockTag = new CompoundTag();
      final BlockPos pos = entry.getKey();
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
