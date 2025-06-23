package net.so_coretech.seismicexploration.entity.ai.task;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.action.ActionStatus;
import net.so_coretech.seismicexploration.entity.ai.action.FollowEntityAction;
import net.so_coretech.seismicexploration.entity.ai.action.IAction;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;
import org.slf4j.Logger;

public class FollowPlayerTask implements ITask {

  private static final Logger LOGGER = LogUtils.getLogger();

  private TaskStatus status;
  private final Player playerToFollow;
  private @Nullable IAction currentAction;

  // Task parameters
  private static final double FOLLOW_SPEED_MODIFIER = 1.1D;
  private static final float FOLLOW_STOP_DISTANCE = 3.0F; // Stop when within 3 blocks
  private static final float MAX_FOLLOW_DISTANCE = 32.0F; // If player is further, task might fail

  public FollowPlayerTask(@Nullable final Player playerToFollow) {
    this.status = TaskStatus.PENDING;
    this.playerToFollow = playerToFollow;
  }

  @Override
  public TaskStatus getStatus() {
    return status;
  }

  @Override
  public void start(final WorkerEntity npc, @Nullable final Player orderingPlayer) {
    if (this.playerToFollow == null) {
      LOGGER.warn(
          "NPC {} ({}) FollowPlayerTask started with no player to follow.",
          npc.getId(),
          npc.getNickname());
      // This task should ideally not be created if playerToFollow is null.
      // The orderingPlayer might be different from playerToFollow if assigned by server logic.
      // For now, we assume orderingPlayer is the one to follow if playerToFollow was null at
      // construction.
      // This part needs careful handling in TaskFactory.
      if (orderingPlayer != null) {
        // This is a fallback, ideally playerToFollow is set correctly at construction.
        // currentAction = new FollowEntityAction(orderingPlayer, FOLLOW_SPEED_MODIFIER,
        // FOLLOW_STOP_DISTANCE, MAX_FOLLOW_DISTANCE);
        // For now, let's stick to the constructor-provided player.
        LOGGER.error(
            "NPC {} ({}) FollowPlayerTask: playerToFollow is null, and orderingPlayer fallback is not fully implemented for action creation here. Task will likely fail.",
            npc.getId(),
            npc.getNickname());
        return; // Cannot start without a target
      } else {
        LOGGER.error(
            "NPC {} ({}) FollowPlayerTask: playerToFollow is null and no orderingPlayer. Cannot start.",
            npc.getId(),
            npc.getNickname());
        return;
      }
    }

    LOGGER.info(
        "NPC {} ({}) starting FollowPlayerTask for player {}.",
        npc.getId(),
        npc.getNickname(),
        this.playerToFollow.getName().getString());
    this.currentAction =
        new FollowEntityAction(
            this.playerToFollow, FOLLOW_SPEED_MODIFIER, FOLLOW_STOP_DISTANCE, MAX_FOLLOW_DISTANCE);
    this.currentAction.start(npc, orderingPlayer); // orderingPlayer is passed for context

    if (orderingPlayer != null) {
      // Using displayClientMessage as an alternative to sendSystemMessage
      orderingPlayer.displayClientMessage(
          Component.literal(String.format("<%s> Alright, I'll follow you!", npc.getNickname())),
          false); // false for chat, true for action bar
    }
  }

  @Override
  public TaskStatus tick(final WorkerEntity npc) {
    if (this.playerToFollow == null || !this.playerToFollow.isAlive()) {
      LOGGER.info(
          "NPC {} ({}) FollowPlayerTask: Player {} is no longer valid. Task failed.",
          npc.getId(),
          npc.getNickname(),
          (this.playerToFollow != null ? this.playerToFollow.getName().getString() : "null"));
      if (currentAction != null) {
        currentAction.stop(npc, ActionStatus.FAILURE);
        currentAction = null;
      }
      status = TaskStatus.FAILURE;
      return status;
    }

    if (currentAction == null) {
      // This might happen if start failed or an action completed unexpectedly.
      LOGGER.warn(
          "NPC {} ({}) FollowPlayerTask: currentAction is null during tick.",
          npc.getId(),
          npc.getNickname());
      status = TaskStatus.FAILURE;
      return status; // Or re-initialize? For follow, probably failure.
    }

    final ActionStatus actionStatus = currentAction.tick(npc);

    if (actionStatus == ActionStatus.FAILURE) {
      LOGGER.info(
          "NPC {} ({}) FollowPlayerTask: FollowEntityAction failed for player {}.",
          npc.getId(),
          npc.getNickname(),
          this.playerToFollow.getName().getString());
      currentAction.stop(npc, ActionStatus.FAILURE); // Ensure stop is called
      currentAction = null;
      status = TaskStatus.FAILURE; // Set task status to failure
      return status;
    }

    // FollowPlayerTask is continuous until explicitly stopped or player becomes invalid.
    // It doesn't have a natural "SUCCESS" state like deploying N items.
    status = TaskStatus.RUNNING;
    return status;
  }

  @Override
  public void stop(final WorkerEntity npc, final TaskStatus status) {
    LOGGER.info(
        "NPC {} ({}) stopping FollowPlayerTask for player {} with status: {}",
        npc.getId(),
        npc.getNickname(),
        (this.playerToFollow != null ? this.playerToFollow.getName().getString() : "null"),
        status);
    if (currentAction != null) {
      currentAction.stop(
          npc,
          status == TaskStatus.SUCCESS
              ? ActionStatus.SUCCESS
              : ActionStatus.FAILURE); // Map task status
      currentAction = null;
    }
    // Send completion message only if it wasn't a failure or cancellation by new task
    if (status != TaskStatus.FAILURE
        && status != TaskStatus.CANCELLED_BY_NEW_TASK
        && status != TaskStatus.INTERRUPTED
        && status != TaskStatus.INTERRUPTED_BY_FLEE) {
      // Follow task doesn't usually "complete" with a success message,
      // it's usually overridden by another task.
      // But if it were to stop "naturally" (e.g. player logs off, handled by tick),
      // a message might be relevant.
      // For now, no specific "stopped following" message unless it's a failure.
    }
  }

  @Override
  public OrderType getOrderType() {
    return OrderType.FOLLOW_ME;
  }

  @Override
  public void onDamaged(
      final WorkerEntity npc,
      final DamageSource source,
      final float amount,
      @Nullable final Player orderingPlayer) {
    // The FleeWhenAttackedGoal will handle the primary reaction.
    // This task itself doesn't need to do much more, but could log or slightly alter behavior.
    LOGGER.debug(
        "NPC {} ({}) (FollowPlayerTask for {}) was damaged by {}.",
        npc.getId(),
        npc.getNickname(),
        (this.playerToFollow != null ? this.playerToFollow.getName().getString() : "null"),
        source.getMsgId());
    // If the player being followed is the attacker, maybe stop following?
    if (source.getEntity() == this.playerToFollow) {
      LOGGER.info(
          "NPC {} ({}) stopped following player {} because they attacked it.",
          npc.getId(),
          npc.getNickname(),
          this.playerToFollow.getName().getString());
      // This would require the task to be able to signal its own failure/stop.
      // For now, let FleeWhenAttackedGoal handle it.
    }
  }
}
