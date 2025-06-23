package net.so_coretech.seismicexploration.entity.ai.task;

import com.mojang.logging.LogUtils;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.action.ActionStatus;
import net.so_coretech.seismicexploration.entity.ai.action.IAction;
import net.so_coretech.seismicexploration.entity.ai.action.MoveToPositionAction;
import net.so_coretech.seismicexploration.entity.ai.action.PickUpItemAction;
import net.so_coretech.seismicexploration.entity.ai.action.PlaceItemAction;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;
import org.slf4j.Logger;

public class DeployTask implements ITask {

  private static final Logger LOGGER = LogUtils.getLogger();

  private TaskStatus status;
  private final Item itemToDeploy;
  private final BlockPos startPos;
  private final Direction direction;
  private final int count;
  private final int gap;
  private final OrderType orderType; // To distinguish between SENSORS and CHARGES if needed

  private final Queue<IAction> actionQueue = new LinkedList<>();
  private @Nullable IAction currentAction;
  private @Nullable Player orderingPlayerCache; // Cache for messages on stop

  // Parameters for actions
  private static final boolean FETCH_FROM_CONTAINERS = true;
  private static final int CONTAINER_SEARCH_RADIUS = 16;
  private static final double MOVEMENT_SPEED = 1.0D;
  private static final float MOVEMENT_ACCEPTANCE_RADIUS = 1.5F;

  public DeployTask(
      final Item itemToDeploy,
      final BlockPos startPos,
      final Direction direction,
      final int count,
      final int gap,
      final OrderType orderType) {
    this.status = TaskStatus.PENDING;
    this.itemToDeploy = itemToDeploy;
    this.startPos = startPos;
    this.direction = direction;
    this.count = count;
    this.gap = gap;
    this.orderType = orderType;
  }

  @Override
  public TaskStatus getStatus() {
    return status;
  }

  @Override
  public void start(final WorkerEntity npc, @Nullable final Player orderingPlayer) {
    this.orderingPlayerCache = orderingPlayer;
    LOGGER.info(
        "NPC {} ({}) starting DeployTask: Deploy {}x {} from {} towards {} (gap {}).",
        npc.getNickname(),
        npc.getId(),
        count,
        itemToDeploy.getDescriptionId(),
        startPos,
        direction.getName(),
        gap);

    if (orderingPlayer != null) {
      orderingPlayer.displayClientMessage(
          Component.translatable(
              "message.seismicexploration.deploy_order_received",
              npc.getNickname(),
              count,
              Component.translatable(itemToDeploy.getDescriptionId())),
          false);
    }

    // 1. Pick up required items
    actionQueue.add(
        new PickUpItemAction(itemToDeploy, count, FETCH_FROM_CONTAINERS, CONTAINER_SEARCH_RADIUS));

    // 2. Move to the actual starting position for deployment
    // We might want to adjust startPos to be on the ground.
    final BlockPos actualStartPos = getGroundLevel(npc.level(), this.startPos);
    actionQueue.add(
        new MoveToPositionAction(
            actualStartPos, MOVEMENT_SPEED, MOVEMENT_ACCEPTANCE_RADIUS, false));

    // 3. Deploy items in a loop
    BlockPos currentDeploymentPos = actualStartPos;
    for (int i = 0; i < count; i++) {
      final BlockPos placePos = getGroundLevel(npc.level(), currentDeploymentPos);
      actionQueue.add(new PlaceItemAction(itemToDeploy, placePos));
      if (i < count - 1) { // Don't move after placing the last item in this sequence
        currentDeploymentPos = currentDeploymentPos.relative(direction, gap + 1);
        final BlockPos nextMoveTarget = getGroundLevel(npc.level(), currentDeploymentPos);
        actionQueue.add(
            new MoveToPositionAction(
                nextMoveTarget, MOVEMENT_SPEED, MOVEMENT_ACCEPTANCE_RADIUS, true));
      }
    }

    // 4. Return to the (actual) start position
    actionQueue.add(
        new MoveToPositionAction(
            actualStartPos, MOVEMENT_SPEED, MOVEMENT_ACCEPTANCE_RADIUS, false));

    startNextAction(npc);
  }

  /*
   * Custom implementation of isAir(). A block is also considered as air if it
   * is walkable.
   */
  private boolean isAir(final Level level, final BlockPos pos) {
    final BlockState state = level.getBlockState(pos);
    final VoxelShape shape = state.getShape(level, startPos);
    return state.isAir() || state.canBeReplaced() || shape.isEmpty() || shape.max(Axis.Y) < 0.5;
  }

  private BlockPos getGroundLevel(final Level level, final BlockPos pos) {
    // Simple "highest solid block" logic. Could be more sophisticated.
    BlockPos currentPos = new BlockPos(pos.getX(), level.getHeight(), pos.getZ());
    while (currentPos.getY() > level.getMinY()) {
      if (!isAir(level, currentPos.below()) && isAir(level, currentPos)) { // Found ground
        return currentPos;
      }
      currentPos = currentPos.below();
    }
    return pos; // Fallback
  }

  private void startNextAction(final WorkerEntity npc) {
    if (actionQueue.isEmpty()) {
      currentAction = null;
      LOGGER.info("NPC {} ({}) DeployTask: Action queue is empty.", npc.getId(), npc.getNickname());
      return;
    }
    currentAction = actionQueue.poll();
    LOGGER.debug(
        "NPC {} ({}) DeployTask: Starting next action: {}",
        npc.getId(),
        npc.getNickname(),
        currentAction.getDebugName());
    currentAction.start(npc, this.orderingPlayerCache);
  }

  @Override
  public TaskStatus tick(final WorkerEntity npc) {
    if (currentAction == null) {
      if (actionQueue.isEmpty()) {
        LOGGER.info(
            "NPC {} ({}) DeployTask: Completed all actions successfully.",
            npc.getId(),
            npc.getNickname());
        status = TaskStatus.SUCCESS;
        return status;
      } else {
        // Should not happen if startNextAction was called correctly
        LOGGER.warn(
            "NPC {} ({}) DeployTask: currentAction is null but queue is not empty. Attempting to start next.",
            npc.getId(),
            npc.getNickname());
        startNextAction(npc);
        if (currentAction == null) {
          status = TaskStatus.FAILURE;
          return status; // Still null, something is wrong
        }
      }
    }

    final ActionStatus actionStatus = currentAction.tick(npc);

    switch (actionStatus) {
      case RUNNING:
        status = TaskStatus.RUNNING;
        return status;
      case SUCCESS:
        LOGGER.debug(
            "NPC {} ({}) DeployTask: Action {} completed successfully.",
            npc.getId(),
            npc.getNickname(),
            currentAction.getDebugName());
        currentAction.stop(npc, ActionStatus.SUCCESS); // Ensure stop is called
        startNextAction(npc);
        status =
            currentAction == null && actionQueue.isEmpty()
                ? TaskStatus.SUCCESS
                : TaskStatus.RUNNING;
        return status;
      case FAILURE:
        LOGGER.warn(
            "NPC {} ({}) DeployTask: Action {} failed. Aborting task.",
            npc.getId(),
            npc.getNickname(),
            currentAction.getDebugName());
        currentAction.stop(npc, ActionStatus.FAILURE); // Ensure stop is called
        currentAction = null;
        actionQueue.clear(); // Clear remaining actions
        status = TaskStatus.FAILURE;
        return status;
      default:
        LOGGER.error(
            "NPC {} ({}) DeployTask: Unknown ActionStatus {}.",
            npc.getId(),
            npc.getNickname(),
            actionStatus);
        status = TaskStatus.FAILURE;
        return status;
    }
  }

  @Override
  public void stop(final WorkerEntity npc, final TaskStatus status) {
    LOGGER.info(
        "NPC {} ({}) stopping DeployTask for {} with status: {}",
        npc.getId(),
        npc.getNickname(),
        itemToDeploy.getDescriptionId(),
        status);
    if (currentAction != null) {
      currentAction.stop(npc, ActionStatus.FAILURE); // Assume failure if task is stopped externally
      currentAction = null;
    }
    actionQueue.clear();

    if (orderingPlayerCache != null) {
      final Component message;
      final Component itemNameComponent = Component.translatable(itemToDeploy.getDescriptionId());
      if (status == TaskStatus.SUCCESS) {
        message =
            Component.translatable(
                "message.seismicexploration.deploy_complete", npc.getNickname(), itemNameComponent);
      } else if (status == TaskStatus.FAILURE) {
        message =
            Component.translatable(
                "message.seismicexploration.deploy_failed", npc.getNickname(), itemNameComponent);
      } else {
        message =
            Component.translatable(
                "message.seismicexploration.deploy_cancelled",
                npc.getNickname(),
                itemNameComponent);
      }
      orderingPlayerCache.displayClientMessage(message, false);
    }
    this.orderingPlayerCache = null; // Clear cache
  }

  @Override
  public OrderType getOrderType() {
    return this.orderType;
  }

  @Override
  public void onDamaged(
      final WorkerEntity npc,
      final DamageSource source,
      final float amount,
      @Nullable final Player orderingPlayer) {
    LOGGER.debug(
        "NPC {} ({}) (DeployTask for {}) was damaged by {}. Task will be interrupted by FleeGoal if necessary.",
        npc.getId(),
        npc.getNickname(),
        itemToDeploy.getDescriptionId(),
        source.getMsgId());
    // FleeWhenAttackedGoal will handle interruption. This task doesn't need to do more.
  }
}
