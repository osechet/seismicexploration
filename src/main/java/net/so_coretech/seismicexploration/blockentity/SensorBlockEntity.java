package net.so_coretech.seismicexploration.blockentity;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.block.SensorBlock;
import net.so_coretech.seismicexploration.util.BlockFinder;
import org.slf4j.Logger;

public class SensorBlockEntity extends BlockEntity implements TickableBlockEntity {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final Set<BlockPos> blocks = new HashSet<>();

  public SensorBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.SENSOR_ENTITY.get(), pos, state);
  }

  public Set<BlockPos> getBlocks() {
    return Collections.unmodifiableSet(blocks);
  }

  public void record(final BlockPos sourcePos) {
    LOGGER.debug("Sensor at {} starting recording for source at {}", worldPosition, sourcePos);
    SensorBlock sensorBlock = (SensorBlock) level.getBlockState(worldPosition).getBlock();
    final int radius = sensorBlock.getRadius();
    final double angle = sensorBlock.getReflectionAngle();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        BlockPos source = sourcePos.offset(dx, 0, dz);
        BlockPos sensor = worldPosition.offset(dx, 0, dz);
        Vec3i cdp = BlockFinder.getDepthPoint(sourcePos, sensor, angle);
        Consumer<? super Vec3i> action =
            pos -> {
              if (!blocks.contains(pos)) {
                blocks.add(new BlockPos(pos));
              }
            };
        BlockFinder.listBlocks(source, cdp).forEach(action);
        BlockFinder.listBlocks(sensor, cdp).forEach(action);
      }
    }
    setChanged();
  }

  @Override
  public void tick() {}

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
        blocks.add(new BlockPos(x.get(), y.get(), z.get()));
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
