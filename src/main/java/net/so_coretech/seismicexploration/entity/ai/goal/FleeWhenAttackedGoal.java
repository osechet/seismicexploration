package net.so_coretech.seismicexploration.entity.ai.goal;

import com.mojang.logging.LogUtils;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.task.ITask;
// TaskFactory no longer needed here for free roam transition
import net.so_coretech.seismicexploration.entity.ai.task.TaskStatus;
import org.slf4j.Logger;

public class FleeWhenAttackedGoal extends Goal {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final WorkerEntity worker;
  private LivingEntity attacker;
  private Player playerToFleeTowards;
  private final double speedModifier = 1.2D; // Flee a bit faster

  public FleeWhenAttackedGoal(final WorkerEntity worker) {
    this.worker = worker;
    this.setFlags(EnumSet.of(Goal.Flag.MOVE));
  }

  @Override
  public boolean canUse() {
    this.attacker = worker.getLastHurtByMob();
    if (this.attacker == null || !this.attacker.isAlive()) {
      return false;
    }
    this.playerToFleeTowards = worker.getTaskOrderingPlayer();
    if (this.playerToFleeTowards == null || !this.playerToFleeTowards.isAlive()) {
      return false; // No player to flee to
    }
    // Only flee if the player is reasonably close, otherwise, it might be a lost cause
    if (worker.distanceToSqr(this.playerToFleeTowards) > 256.0D) { // 16 blocks
      return false;
    }
    LOGGER.debug(
        "Worker {} can use FleeWhenAttackedGoal. Attacker: {}, Fleeing to: {}",
        worker.getId(),
        attacker.getName().getString(),
        playerToFleeTowards.getName().getString());
    return true;
  }

  @Override
  public void start() {
    LOGGER.info(
        "Worker {} starting FleeWhenAttackedGoal. Fleeing from {} towards {}.",
        worker.getId(),
        attacker.getName().getString(),
        playerToFleeTowards.getName().getString());

    final ITask currentTask = worker.getCurrentTask();
    if (currentTask != null) {
      LOGGER.debug(
          "Worker {} notifying current task {} about being attacked.",
          worker.getId(),
          currentTask.getClass().getSimpleName());
      // The task's onDamaged might decide to stop itself.
      // If not, this goal will take over and interrupt it.
      currentTask.onDamaged(
          worker, worker.getLastDamageSource(), worker.getLastDamageTaken(), playerToFleeTowards);

      // If the task didn't stop itself, forcibly stop it.
      if (worker.getCurrentTask() == currentTask) { // Check if task is still the same
        LOGGER.debug(
            "Worker {} current task {} did not stop itself, interrupting with INTERRUPTED_BY_FLEE.",
            worker.getId(),
            currentTask.getClass().getSimpleName());
        currentTask.stop(worker, TaskStatus.INTERRUPTED_BY_FLEE);
        // Clear the task from the worker if it was forcibly stopped by flee.
        // The ExecuteComplexTaskGoal won't run if this goal is active.
        worker.assignTask(null, null); // This ensures the old task is fully cleared
      }
    }
    worker.getNavigation().moveTo(playerToFleeTowards, speedModifier);
  }

  @Override
  public boolean canContinueToUse() {
    if (this.attacker == null || !this.attacker.isAlive()) {
      return false; // Attacker gone
    }
    if (this.playerToFleeTowards == null || !this.playerToFleeTowards.isAlive()) {
      return false; // Player to flee to gone
    }
    if (worker.getNavigation().isDone()) {
      return false; // Reached destination (or couldn't path)
    }
    // Stop if too far from the player we are fleeing towards
    return worker.distanceToSqr(this.playerToFleeTowards) <= 512.0D; // ~22 blocks
  }

  @Override
  public void stop() {
    LOGGER.info("Worker {} stopping FleeWhenAttackedGoal.", worker.getId());
    this.attacker = null;
    this.playerToFleeTowards = null;
    worker.getNavigation().stop();

    // After fleeing, clear the current task to allow default behaviors (e.g., free roam)
    LOGGER.debug(
        "Worker {} finished fleeing. Clearing current task to allow default behaviors.",
        worker.getId());
    // The orderingPlayer for "no task" is null.
    worker.assignTask(null, null);
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true; // Keep pathing updated
  }
}
