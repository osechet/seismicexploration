package net.so_coretech.seismicexploration.entity.ai.action;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public class FollowEntityAction implements IAction {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final LivingEntity target;
  private final double speedModifier;
  private final float stopDistance; // Distance at which to stop, squared
  private final float maxDistanceToTeleport; // If target is too far, consider action failed/stopped
  private int updatePathDelay;

  public FollowEntityAction(
      final LivingEntity target,
      final double speedModifier,
      final float stopDistance,
      final float maxDistanceToTeleport) {
    this.target = target;
    this.speedModifier = speedModifier;
    this.stopDistance = stopDistance * stopDistance; // Compare squared distances
    this.maxDistanceToTeleport = maxDistanceToTeleport * maxDistanceToTeleport;
  }

  @Override
  public boolean allowFailure() {
    return false;
  }

  @Override
  public void start(final WorkerEntity npc, @Nullable final Player orderingPlayer) {
    LOGGER.debug(
        "NPC {} ({}) starting FollowEntityAction, target: {}",
        npc.getId(),
        npc.getNickname(),
        target.getName().getString());
    this.updatePathDelay = 0;
    final PathNavigation navigation = npc.getNavigation();
    navigation.stop(); // Stop any previous movement
    // Attempt to move to target immediately
    if (!navigation.moveTo(this.target, this.speedModifier)) {
      LOGGER.warn(
          "NPC {} ({}) could not create initial path to target {} in FollowEntityAction.",
          npc.getId(),
          npc.getNickname(),
          target.getName().getString());
    }
  }

  @Override
  public ActionStatus tick(final WorkerEntity npc) {
    if (this.target == null || !this.target.isAlive()) {
      LOGGER.debug(
          "NPC {} ({}) FollowEntityAction: Target is null or not alive.",
          npc.getId(),
          npc.getNickname());
      return ActionStatus.FAILURE;
    }

    if (npc.distanceToSqr(this.target) > this.maxDistanceToTeleport) {
      LOGGER.debug(
          "NPC {} ({}) FollowEntityAction: Target {} is too far away ({} > {}). Action failed.",
          npc.getId(),
          npc.getNickname(),
          target.getName().getString(),
          npc.distanceToSqr(this.target),
          this.maxDistanceToTeleport);
      return ActionStatus.FAILURE; // Target too far
    }

    npc.getLookControl().setLookAt(this.target, 10.0F, (float) npc.getMaxHeadXRot());

    this.updatePathDelay--;
    if (this.updatePathDelay <= 0) {
      this.updatePathDelay = 10; // Recalculate path every 10 ticks
      if (!npc.getNavigation().moveTo(this.target, this.speedModifier)) {
        // If pathing fails repeatedly, might consider failure, but for follow, keep trying
        LOGGER.debug(
            "NPC {} ({}) FollowEntityAction: Path to {} failed to update. Will retry.",
            npc.getId(),
            npc.getNickname(),
            target.getName().getString());
      }
    }

    // Check if close enough
    if (npc.distanceToSqr(this.target) <= this.stopDistance) {
      // If close enough and not actively pathing (or path is very short), consider it success for
      // this tick
      // For a continuous follow, this action might always return RUNNING unless interrupted.
      // However, for a task system, it might be better to return SUCCESS when "at" the target.
      // Let's make it RUNNING as long as the target is valid and within reasonable distance.
      // The ITask managing this action will decide when "following" is truly "done".
      if (!npc.getNavigation().isDone()
          && npc.getNavigation().getPath() != null
          && npc.getNavigation().getPath().getDistToTarget() > (this.stopDistance / 2.0f)) {
        // Still moving towards target
      } else {
        // Close enough, stop active pathing to prevent jitter
        npc.getNavigation().stop();
      }
    }
    return ActionStatus.RUNNING; // Follow is a continuous action
  }

  @Override
  public void stop(final WorkerEntity npc, final ActionStatus status) {
    LOGGER.debug(
        "NPC {} ({}) stopping FollowEntityAction for target {} with status: {}",
        npc.getId(),
        npc.getNickname(),
        target.getName().getString(),
        status);
    // Only stop navigation if the NPC is not already pathing for some other reason
    // or if the goal is ending. Since this is an action, it should clean up its own pathing.
    if (npc.getNavigation().getTargetPos() == null
        || npc.getNavigation().getTargetPos().equals(target.blockPosition())
        || status != ActionStatus.RUNNING) { // if not running, it's a definitive stop
      npc.getNavigation().stop();
    }
  }

  @Override
  public String getDebugName() {
    return "FollowEntityAction[target=" + target.getName().getString() + "]";
  }
}
