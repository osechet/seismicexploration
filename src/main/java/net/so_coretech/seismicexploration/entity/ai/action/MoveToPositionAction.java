package net.so_coretech.seismicexploration.entity.ai.action;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public class MoveToPositionAction implements IAction {

  private static final Logger LOGGER = LogUtils.getLogger();
  private static final int MAX_RETRIES = 3; // Maximum retries for pathfinding

  private final BlockPos targetPos;
  private final double speedModifier;
  private final float acceptanceRadius; // How close to get to the target
  private final boolean allowFailure; // Whether this action can fail without affecting the task
  private int retryCount = 0;

  public MoveToPositionAction(
      final BlockPos targetPos,
      final double speedModifier,
      final float acceptanceRadius,
      final boolean allowFailure) {
    this.targetPos = targetPos;
    this.speedModifier = speedModifier;
    this.acceptanceRadius = acceptanceRadius;
    this.allowFailure = allowFailure;
  }

  @Override
  public boolean allowFailure() {
    return allowFailure;
  }

  @Override
  public void start(final WorkerEntity npc, @Nullable final Player orderingPlayer) {
    LOGGER.debug(
        "NPC {} ({}) starting MoveToPositionAction, target: {}, speed: {}",
        npc.getId(),
        npc.getNickname(),
        this.targetPos,
        this.speedModifier);
    final PathNavigation navigation = npc.getNavigation();
    navigation.stop(); // Stop any previous movement
    if (!navigation.moveTo(
        this.targetPos.getX() + 0.5D,
        this.targetPos.getY(),
        this.targetPos.getZ() + 0.5D,
        this.speedModifier)) {
      LOGGER.warn(
          "NPC {} ({}) could not create initial path to target {} in MoveToPositionAction.",
          npc.getId(),
          npc.getNickname(),
          this.targetPos);
      // The tick method will handle failure if pathing remains impossible.
    }
  }

  @Override
  public ActionStatus tick(final WorkerEntity npc) {
    if (this.targetPos == null) {
      LOGGER.warn(
          "NPC {} ({}) MoveToPositionAction: targetPos is null.", npc.getId(), npc.getNickname());
      return allowFailure ? ActionStatus.SUCCESS : ActionStatus.FAILURE;
    }

    final PathNavigation navigation = npc.getNavigation();
    if (navigation.isDone()
        || npc.blockPosition().closerThan(this.targetPos, this.acceptanceRadius)) {
      // If navigation is done (either reached or failed) or we are close enough
      if (npc.blockPosition().closerThan(this.targetPos, this.acceptanceRadius)) {
        LOGGER.debug(
            "NPC {} ({}) MoveToPositionAction: Reached target {} (or close enough).",
            npc.getId(),
            npc.getNickname(),
            this.targetPos);
        navigation.stop(); // Ensure NPC stops moving
        return ActionStatus.SUCCESS;
      } else {
        // Navigation is done, but not close enough - path likely failed.
        if (retryCount < MAX_RETRIES) {
          LOGGER.warn(
              "NPC {} ({}) MoveToPositionAction: Navigation finished but not at target {}. Retrying.",
              npc.getId(),
              npc.getNickname(),
              this.targetPos);
          retryCount++;
          if (!navigation.moveTo(
              this.targetPos.getX() + 0.5D,
              this.targetPos.getY(),
              this.targetPos.getZ() + 0.5D,
              this.speedModifier)) {
            LOGGER.warn(
                "NPC {} ({}) could not create initial path to target {} in MoveToPositionAction.",
                npc.getId(),
                npc.getNickname(),
                this.targetPos);
            // The tick method will handle failure if pathing remains impossible.
          }
          return ActionStatus.RUNNING;
        } else {
          LOGGER.warn(
              "NPC {} ({}) MoveToPositionAction: Failed to reach target {} after retries.",
              npc.getId(),
              npc.getNickname(),
              this.targetPos);
          return allowFailure
              ? ActionStatus.SUCCESS
              : ActionStatus.FAILURE; // Failed to path after retries
        }
      }
    }

    // If still pathing, keep looking at the target (or slightly above it)
    npc.getLookControl().setLookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
    retryCount = 0; // Reset retry count since we're still moving

    return ActionStatus.RUNNING;
  }

  @Override
  public void stop(final WorkerEntity npc, final ActionStatus status) {
    LOGGER.debug(
        "NPC {} ({}) stopping MoveToPositionAction for target {} with status: {}",
        npc.getId(),
        npc.getNickname(),
        this.targetPos,
        status);
    // Only stop navigation if the action didn't succeed or if it's a definitive stop.
    // If SUCCESS, navigation should already be stopped or very close to target.
    if (status != ActionStatus.SUCCESS) {
      // Check if the current path target is still this action's target
      BlockPos navTarget = npc.getNavigation().getTargetPos();
      if (navTarget != null && navTarget.equals(this.targetPos)) {
        npc.getNavigation().stop();
      }
    }
  }

  @Override
  public String getDebugName() {
    return "MoveToPositionAction[target=" + targetPos + "]";
  }
}
