package net.so_coretech.seismicexploration.entity.ai.goal;

import com.mojang.logging.LogUtils;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.task.ITask;
// TaskFactory and FreeRoamTask no longer directly needed here for free roam transition
import net.so_coretech.seismicexploration.entity.ai.task.TaskStatus;
import org.slf4j.Logger;

public class ExecuteComplexTaskGoal extends Goal {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final WorkerEntity worker;

  public ExecuteComplexTaskGoal(final WorkerEntity worker) {
    this.worker = worker;
    this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); // Tasks might involve movement and looking
  }

  @Override
  public boolean canUse() {
    final ITask currentTask = worker.getCurrentTask();
    if (currentTask == null) {
      return false;
    }
    // This goal should run if there's any active task.
    // If currentTask is null, this goal won't run, and lower priority goals (like RandomStrollGoal)
    // will.
    return currentTask != null;
  }

  @Override
  public boolean canContinueToUse() {
    // Continue if the task is still active.
    return worker.getCurrentTask() != null;
  }

  @Override
  public void start() {
    final ITask currentTask = worker.getCurrentTask();
    if (currentTask != null) {
      LOGGER.debug(
          "Worker {} starting ExecuteComplexTaskGoal for task: {}",
          worker.getId(),
          currentTask.getClass().getSimpleName());
    } else {
      LOGGER.warn(
          "Worker {} ExecuteComplexTaskGoal started but currentTask is null.", worker.getId());
    }
  }

  @Override
  public void stop() {
    final ITask currentTask = worker.getCurrentTask();
    if (currentTask != null) {
      // If the goal is stopped externally (e.g., by a higher priority goal),
      // ensure the task is also notified to stop.
      LOGGER.debug(
          "Worker {} stopping ExecuteComplexTaskGoal. Current task {} will be stopped with INTERRUPTED.",
          worker.getId(),
          currentTask.getClass().getSimpleName());
      currentTask.stop(worker, TaskStatus.INTERRUPTED);
      // Do not assign FreeRoamTask here, as the interruption might be temporary
      // or handled by the interrupting goal.
    } else {
      LOGGER.debug("Worker {} stopping ExecuteComplexTaskGoal. No current task.", worker.getId());
    }
  }

  @Override
  public void tick() {
    final ITask currentTask = worker.getCurrentTask();
    if (currentTask == null) {
      // Should not happen if canUse() and canContinueToUse() are correct
      LOGGER.warn(
          "Worker {} ExecuteComplexTaskGoal ticked but currentTask is null.", worker.getId());
      return;
    }

    final TaskStatus status = currentTask.tick(worker);

    if (status == TaskStatus.SUCCESS || status == TaskStatus.FAILURE) {
      LOGGER.info(
          "Worker {} task {} completed with status: {}",
          worker.getId(),
          currentTask.getClass().getSimpleName(),
          status);
      // The task itself should call its stop method upon completion/failure.
      // currentTask.stop(worker, status); // This is usually handled by the task's tick logic

      // Transition to Free Roaming by clearing the current task
      LOGGER.debug(
          "Worker {} task {} completed. Clearing current task to allow default behaviors.",
          worker.getId(),
          currentTask.getClass().getSimpleName());
      // Setting task to null will make canUse() and canContinueToUse() false for this goal,
      // allowing RandomStrollGoal or other lower-priority goals to take over.
      // The orderingPlayer for "no task" is null.
      worker.assignTask(null, null);
    }
  }
}
