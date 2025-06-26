package net.so_coretech.seismicexploration.blockentity;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.block.SensorBlock;
import org.slf4j.Logger;

public class SensorBlockEntity extends BlockEntity implements TickableBlockEntity {

  private static final Logger LOGGER = LogUtils.getLogger();
  private static final int ticksToRecord = 40; // 2 seconds at 20 ticks per second

  private @Nullable BlockPos recordingPos;
  private int blocksPerTick = 0;
  private int maxY;

  private final Set<BlockPos> blocks = new HashSet<>();

  public SensorBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.SENSOR_ENTITY.get(), pos, state);
  }

  private int getRadius() {
    return ((SensorBlock) level.getBlockState(worldPosition).getBlock()).getRadius();
  }

  public Set<BlockPos> getBlocks() {
    return Collections.unmodifiableSet(blocks);
  }

  public void startRecording(final BlockPos pos) {
    LOGGER.debug("Sensor at {} starting recording at {}", worldPosition, pos);
    if (level != null) {
      // Calculate how many blocks must be recorded per tick. We limit the number of blocks being
      // browsed per tick to avoid performance issues

      final int radius = getRadius();
      maxY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          int x = pos.getX() + dx;
          int z = pos.getZ() + dz;
          int max = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
          if (max > maxY) {
            maxY = max;
          }
        }
      }
      final int blocksCount = (maxY - pos.getY()) * (int) Math.pow(radius * 2 + 1, 2);
      blocksPerTick = (int) Math.ceil((float) blocksCount / (float) ticksToRecord);
      recordingPos = pos;

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
      if (recordingPos != null) {
        // When recordingPos is set, it means the sensor is recording
        final int radius = getRadius();
        for (int i = 0; i < blocksPerTick; i += Math.pow(radius * 2 + 1, 2)) {
          if (recordingPos.getY() < maxY) {
            LOGGER.trace(
                "Sensor at {} recording layer at y={}", worldPosition, recordingPos.getY());
            int y = recordingPos.getY();
            int centerX = recordingPos.getX();
            int centerZ = recordingPos.getZ();
            for (int dx = -radius; dx <= radius; dx++) {
              for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = new BlockPos(centerX + dx, y, centerZ + dz);
                if (!blocks.contains(pos)) {
                  blocks.add(pos);
                }
              }
            }
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
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
          LOGGER.warn("Invalid block tag");
          continue;
        }
        final BlockPos pos = new BlockPos(x.get(), y.get(), z.get());
        blocks.add(pos);
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
    for (final BlockPos pos : blocks) {
      final CompoundTag blockTag = new CompoundTag();
      blockTag.putInt("x", pos.getX());
      blockTag.putInt("y", pos.getY());
      blockTag.putInt("z", pos.getZ());
      blocksList.add(blockTag);
    }
    compound.put("blocks", blocksList);

    tag.put(SeismicExploration.MODID, compound);
  }
}
