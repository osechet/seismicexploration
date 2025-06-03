package net.so_coretech.seismicexploration.entity.ai.goal;

import com.mojang.logging.LogUtils;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.so_coretech.seismicexploration.ModItems;
import net.so_coretech.seismicexploration.util.InventoryUtils;
import org.slf4j.Logger;

public class DeploySensorsGoal extends Goal {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final PathfinderMob mob;
  private final BlockPos startPos;
  private final Direction direction;
  private final int count;
  private final int gap;
  private final GoalFinishedListener listener;

  private enum Phase {
    CHECK_INVENTORY,
    FETCH_FROM_CONTAINER,
    PLACE_SENSORS,
    RETURN_TO_START,
    DONE
  }

  private Phase phase = Phase.CHECK_INVENTORY;
  private int sensorsDeployed = 0;
  private @Nullable BlockPos nextPos;

  private @Nullable BlockEntity grabContainer;

  public DeploySensorsGoal(
      final PathfinderMob mob,
      final BlockPos startPos,
      final Direction direction,
      final int count,
      final int gap,
      final GoalFinishedListener listener) {
    this.mob = mob;
    this.startPos = getHighestBlock(this.mob.level(), startPos);
    this.direction = direction;
    this.count = count;
    this.gap = gap;
    this.listener = listener;
    this.setFlags(EnumSet.of(Flag.MOVE));
  }

  @Override
  public boolean canUse() {
    // Always allow starting if not already done
    return phase != Phase.DONE;
  }

  @Override
  public void start() {
    phase = Phase.CHECK_INVENTORY;
  }

  @Override
  public void tick() {
    switch (phase) {
      case CHECK_INVENTORY -> handleCheckingInventory();
      case FETCH_FROM_CONTAINER -> handleGrab();
      case PLACE_SENSORS -> handleDeploy();
      case RETURN_TO_START -> handleReturn();
      case DONE -> mob.getNavigation().stop();
    }
  }

  private void handleCheckingInventory() {
    final IItemHandler mobInventory = mob.getCapability(Capabilities.ItemHandler.ENTITY);
    if (mobInventory == null) {
      LOGGER.debug("No inventory found. Aborting.");
      phase = Phase.DONE;
      nextPos = null;
      listener.onGoalFinished("No inventory found. Aborting.");
      return;
    }

    final int inventoryCount = InventoryUtils.countItem(mobInventory, ModItems.DFU.get());
    if (inventoryCount < this.count) {
      LOGGER.debug("Looking for {} DFUs.", this.count - inventoryCount);
      final Optional<BlockEntity> blockEntity =
          InventoryUtils.findContainerWithSensor(
              mob.level(),
              mob.blockPosition(),
              25,
              ModItems.DFU.get(),
              this.count - inventoryCount);
      if (blockEntity.isEmpty()) {
        LOGGER.debug("No DFU found in nearby chests. Aborting.");
        phase = Phase.DONE;
        nextPos = null;
        listener.onGoalFinished("No DFU found in nearby chests. Aborting.");
      } else {
        LOGGER.debug("Going to chest to get more DFUs.");
        phase = Phase.FETCH_FROM_CONTAINER;
        nextPos = blockEntity.get().getBlockPos();
        grabContainer = blockEntity.get();
      }
    } else {
      phase = Phase.PLACE_SENSORS;
      nextPos = startPos;
    }
  }

  private void handleGrab() {
    final double walkingSpeed = 1.0;
    if (nextPos != null && !mob.blockPosition().closerThan(nextPos, 1.2)) {
      LOGGER.debug(
          "At {}, moving to next position: {} ({} blocks away)",
          mob.blockPosition(),
          nextPos,
          mob.blockPosition().distSqr(nextPos));
      mob.getNavigation().moveTo(nextPos.getX(), nextPos.getY(), nextPos.getZ(), walkingSpeed);
    } else {
      if (grabContainer != null) {
        final IItemHandler mobInventory = mob.getCapability(Capabilities.ItemHandler.ENTITY);
        if (mobInventory == null) {
          LOGGER.debug("No inventory found. Aborting.");
          phase = Phase.DONE;
          nextPos = null;
          listener.onGoalFinished("No inventory found. Aborting.");
          return;
        }
        final int inventoryCount = InventoryUtils.countItem(mobInventory, ModItems.DFU.get());
        final int grabbed =
            InventoryUtils.moveItemsBetweenHandlers(
                mob.level()
                    .getCapability(
                        Capabilities.ItemHandler.BLOCK, grabContainer.getBlockPos(), null),
                mobInventory,
                ModItems.DFU.get(),
                this.count - inventoryCount);
        LOGGER.debug("Grabbed {} items from chest", grabbed);
      }
      LOGGER.debug("Got the sensors. Starting deployment.");
      phase = Phase.PLACE_SENSORS;
      nextPos = startPos;
    }
  }

  private void handleDeploy() {
    final IItemHandler mobInventory = mob.getCapability(Capabilities.ItemHandler.ENTITY);
    if (mobInventory == null) {
      LOGGER.debug("No inventory found. Aborting.");
      phase = Phase.DONE;
      nextPos = null;
      listener.onGoalFinished("No inventory found. Aborting.");
      return;
    }

    if (nextPos == null) {
      nextPos = startPos;
    }
    if (!mob.blockPosition().closerThan(nextPos, 1.5)) {
      LOGGER.debug(
          "At {}, moving to next position: {} ({} blocks away)",
          mob.blockPosition(),
          nextPos,
          mob.blockPosition().distSqr(nextPos));
      mob.getNavigation().moveTo(nextPos.getX(), nextPos.getY(), nextPos.getZ(), 1.0);
    } else {
      LOGGER.debug("Arrived at next position. Deploying a sensor.");
      placeSensor(mob.level(), mobInventory, nextPos, ModItems.DFU.get());
      sensorsDeployed++;

      LOGGER.debug("Finding next destination.");
      if (sensorsDeployed < count) {
        nextPos =
            getHighestBlock(this.mob.level(), mob.blockPosition().relative(direction, gap + 1));
        LOGGER.debug(
            "Next destination: {} ({} blocks away)", nextPos, mob.blockPosition().distSqr(nextPos));
      } else {
        LOGGER.debug("All sensors deployed. Starting to return.");
        phase = Phase.RETURN_TO_START;
        nextPos = null;
      }
    }
  }

  private void handleReturn() {
    if (!mob.blockPosition().closerThan(startPos, 1.2)) {
      LOGGER.debug(
          "At {}, returning to start position: {} ({} blocks away)",
          mob.blockPosition(),
          startPos,
          mob.blockPosition().distSqr(startPos));
      mob.getNavigation().moveTo(startPos.getX(), startPos.getY(), startPos.getZ(), 1.0);
    } else {
      LOGGER.debug("Arrived at start position. Task complete.");
      phase = Phase.DONE;
      listener.onGoalFinished("Arrived at start position. Task complete.");
    }
  }

  @Override
  public boolean canContinueToUse() {
    return phase != Phase.DONE;
  }

  @Override
  public void stop() {
    mob.getNavigation().stop();
  }

  private static BlockPos getHighestBlock(final Level level, final BlockPos pos) {
    // Find the first non-air block at (x, z) that is not a full block
    final BlockPos block =
        getHighestBlock(
            level, pos, state -> !state.isAir() && state.getShape(level, pos).bounds().maxY > 0.5);
    return new BlockPos(block.getX(), block.getY() + 1, block.getZ());
  }

  public interface GoalFinishedListener {
    void onGoalFinished(final String reason);
  }

  /**
   * Similar to level.getHighestBlockYAt but uses a predicate to determine which blocks to ignore.
   *
   * @param level the world level.
   * @param pos the position to check.
   * @param isValid a predicate to determine if the block is valid.
   * @return the highest block position that matches the predicate.
   */
  private static BlockPos getHighestBlock(
      final Level level, final BlockPos pos, final Predicate<BlockState> isValid) {
    final int x = pos.getX();
    final int z = pos.getZ();
    final int topY = level.getMaxY();
    for (int y = topY; y >= level.getMinY(); y--) {
      final BlockPos checkPos = new BlockPos(x, y, z);
      final BlockState state = level.getBlockState(checkPos);
      if (isValid.test(state)) {
        return checkPos;
      }
    }
    // Fallback: return original pos if nothing found
    return pos;
  }

  public static void placeSensor(
      final Level level, final IItemHandler from, final BlockPos pos, final Item sensorItem) {
    if (level.isClientSide) return; // Only run on server

    for (int slot = 0; slot < from.getSlots(); slot++) {
      final ItemStack stack = from.getStackInSlot(slot);
      if (stack.is(sensorItem) && stack.getCount() > 0) {
        if (sensorItem instanceof final BlockItem blockItem) {
          final BlockState state = blockItem.getBlock().defaultBlockState();
          // Check if the block at pos is already the same type
          if (level.getBlockState(pos).is(state.getBlock())) {
            // Already placed, do nothing
            return;
          }
          if (level.getBlockState(pos).canBeReplaced()) {
            final boolean placed = level.setBlock(pos, state, 3);
            if (placed) {
              from.extractItem(slot, 1, false);
              // Optionally: play sound, trigger animation, etc.
            }
          }
        }
        break;
      }
    }
  }
}
