package net.so_coretech.seismicexploration.entity.ai.task;

import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.so_coretech.seismicexploration.ModItems; // Assuming DFU is the sensor item for now
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

// FreeRoamTask is no longer used

// TODO: Import other concrete task implementations as they are created
// import net.so_coretech.seismicexploration.entity.ai.task.type.OperateBoomBoxTask;

/**
 * Factory class for creating {@link ITask} instances based on an {@link OrderType} and parameters.
 */
public class TaskFactory {

  /**
   * Creates a new {@link ITask} for the given worker based on the order type and parameters.
   *
   * @param orderType The type of order to create a task for.
   * @param npc The worker entity that will perform the task.
   * @param orderingPlayer The player who initiated the order, if any.
   * @param parameters Additional parameters for configuring the task (e.g., from a network packet).
   *     This can be null if the task requires no parameters beyond the OrderType.
   * @return A new {@link ITask} instance, or null if the orderType is unrecognized or task creation
   *     fails.
   */
  @Nullable
  public static ITask createTask(
      final OrderType orderType,
      final WorkerEntity npc,
      @Nullable final Player orderingPlayer,
      @Nullable final JsonObject parameters) {

    // TODO: Implement task creation logic for each OrderType
    switch (orderType) {
      case FREE_ROAMING:
        // Returning null for FREE_ROAMING will cause the WorkerEntity's currentTask to be set to
        // null,
        // which will then allow the default RandomStrollGoal to take over.
        return null;
      case FOLLOW_ME:
        if (orderingPlayer == null) {
          System.err.println(
              "TaskFactory: Cannot create FollowPlayerTask without an orderingPlayer to follow.");
          return null;
        }
        return new FollowPlayerTask(orderingPlayer);
      case DEPLOY_SENSORS:
      case DEPLOY_CHARGES: // Both use DeployTask, just with different items/params
        if (parameters == null) {
          System.err.println(
              "TaskFactory: Cannot create DeployTask without parameters for " + orderType);
          return null;
        }
        try {
          final int startX = parameters.get("startX").getAsInt();
          final int startY =
              parameters.get("startY").getAsInt(); // Assuming Y is provided or derived
          final int startZ = parameters.get("startZ").getAsInt();
          final BlockPos startPos = new BlockPos(startX, startY, startZ);

          final Direction direction = Direction.byName(parameters.get("direction").getAsString());
          if (direction == null) {
            System.err.println(
                "TaskFactory: Invalid direction for DeployTask: "
                    + parameters.get("direction").getAsString());
            return null;
          }

          final int count = parameters.get("count").getAsInt();
          final int gap = parameters.get("gap").getAsInt();

          final Item itemToDeploy;
          if (orderType == OrderType.DEPLOY_SENSORS) {
            // Assuming DFU is the sensor item. This might need to be more flexible later.
            itemToDeploy = ModItems.DFU.get();
          } else { // DEPLOY_CHARGES
            // TODO: Replace with actual charge item once it exists
            System.err.println(
                "TaskFactory: DEPLOY_CHARGES item not yet defined. Using DFU as placeholder.");
            itemToDeploy = ModItems.DFU.get(); // Placeholder
          }

          return new DeployTask(
              itemToDeploy,
              getHighestBlock(npc.level(), startPos),
              direction,
              count,
              gap,
              orderType);
        } catch (final Exception e) {
          System.err.println(
              "TaskFactory: Error parsing parameters for DeployTask for "
                  + orderType
                  + ": "
                  + e.getMessage());
          e.printStackTrace();
          return null;
        }
      case OPERATE_BOOM_BOX:
        // TODO: Extract params from JsonObject for OPERATE_BOOM_BOX
        // TODO: return new OperateBoomBoxTask(params);
        System.err.println("TaskFactory: OPERATE_BOOM_BOX task not yet implemented.");
        break; // Fall-through to default or return null if not implemented
      default:
        // Log an error for unhandled order type if it wasn't OPERATE_BOOM_BOX (which has its own
        // message)
        if (orderType != OrderType.OPERATE_BOOM_BOX) {
          System.err.println("TaskFactory: Unhandled OrderType: " + orderType);
        }
        return null; // Return null for unhandled or not-yet-implemented tasks
    }
    // This part of the code will only be reached if an OrderType has a 'break' without a 'return'
    // (e.g., OPERATE_BOOM_BOX if not yet returning a task).
    System.err.println(
        "TaskFactory: No task returned for OrderType (fall-through): "
            + orderType
            + ". Returning null.");
    return null;
  }

  // TODO: merge with DeployTask.getGroundLevel
  private static BlockPos getHighestBlock(final Level level, final BlockPos pos) {
    // Find the first non-air block at (x, z) that is not a full block
    final BlockPos block =
        getHighestBlock(
            level,
            pos,
            state ->
                !state.isAir()
                    && !state.getShape(level, pos).isEmpty()
                    && !state.canBeReplaced()
                    && state.getShape(level, pos).bounds().maxY > 0.5);
    return new BlockPos(block.getX(), block.getY() + 1, block.getZ());
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
}
