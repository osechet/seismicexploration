package net.so_coretech.seismicexploration.blockentity;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.block.SensorBlock;
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
        BlockPos cdp = getDepthPoint(sourcePos, sensor, angle);
        Consumer<? super BlockPos> action =
            pos -> {
              if (!blocks.contains(pos)) {
                blocks.add(pos);
              }
            };
        listBlocks(source, cdp).forEach(action);
        listBlocks(sensor, cdp).forEach(action);
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

  // Calculate the common depth point (CDP) between the source position and the sensor position.
  // For simplification, we assume the reflection always has the given angle (in radians).
  protected static BlockPos getDepthPoint(
      final BlockPos sourcePos, final BlockPos sensorPos, double alpha) {
    final int midX = (sensorPos.getX() + sourcePos.getX()) / 2;
    final int midY = (sensorPos.getY() + sourcePos.getY()) / 2;
    final int midZ = (sensorPos.getZ() + sourcePos.getZ()) / 2;

    // Find the common middle point (CMP) between the source and this block's position
    BlockPos midpoint = new BlockPos(midX, midY, midZ);

    // Calculate the base length (distance between sourcePos and this block)
    double dx = sensorPos.getX() - sourcePos.getX();
    double dy = sensorPos.getY() - sourcePos.getY();
    double dz = sensorPos.getZ() - sourcePos.getZ();
    double base = Math.sqrt(dx * dx + dy * dy + dz * dz);

    // Height of isosceles triangle with base and 60 degree apex
    double height = base / (2 * Math.tan(alpha / 2));

    // Subtract height from median's y
    int newY = (int) Math.round(midpoint.getY() - height);
    return new BlockPos(midpoint.getX(), newY, midpoint.getZ());
  }

  // Returns all block positions along the line from source to dest (inclusive) using Bresenham's 3D
  // algorithm.
  protected static List<BlockPos> listBlocks(BlockPos source, BlockPos dest) {
    List<BlockPos> blocks = new ArrayList<>();
    int x1 = source.getX(), y1 = source.getY(), z1 = source.getZ();
    int x2 = dest.getX(), y2 = dest.getY(), z2 = dest.getZ();

    int dx = Math.abs(x2 - x1);
    int dy = Math.abs(y2 - y1);
    int dz = Math.abs(z2 - z1);

    int sx = Integer.compare(x2, x1);
    int sy = Integer.compare(y2, y1);
    int sz = Integer.compare(z2, z1);

    int x = x1, y = y1, z = z1;
    int n = 1 + dx + dy + dz;
    int dx2 = dx * 2, dy2 = dy * 2, dz2 = dz * 2;
    int err1, err2;

    if (dx >= dy && dx >= dz) {
      err1 = dy2 - dx;
      err2 = dz2 - dx;
      for (int i = 0; i < n; i++) {
        blocks.add(new BlockPos(x, y, z));
        if (x == x2 && y == y2 && z == z2) break;
        if (err1 > 0) {
          y += sy;
          err1 -= dx2;
        }
        if (err2 > 0) {
          z += sz;
          err2 -= dx2;
        }
        err1 += dy2;
        err2 += dz2;
        x += sx;
      }
    } else if (dy >= dx && dy >= dz) {
      err1 = dx2 - dy;
      err2 = dz2 - dy;
      for (int i = 0; i < n; i++) {
        blocks.add(new BlockPos(x, y, z));
        if (x == x2 && y == y2 && z == z2) break;
        if (err1 > 0) {
          x += sx;
          err1 -= dy2;
        }
        if (err2 > 0) {
          z += sz;
          err2 -= dy2;
        }
        err1 += dx2;
        err2 += dz2;
        y += sy;
      }
    } else {
      err1 = dy2 - dz;
      err2 = dx2 - dz;
      for (int i = 0; i < n; i++) {
        blocks.add(new BlockPos(x, y, z));
        if (x == x2 && y == y2 && z == z2) break;
        if (err1 > 0) {
          y += sy;
          err1 -= dz2;
        }
        if (err2 > 0) {
          x += sx;
          err2 -= dz2;
        }
        err1 += dy2;
        err2 += dx2;
        z += sz;
      }
    }
    LOGGER.debug("Recording {} blocks", blocks.size());
    return blocks;
  }
}
