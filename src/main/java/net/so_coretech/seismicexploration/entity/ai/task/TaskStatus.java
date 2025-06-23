package net.so_coretech.seismicexploration.entity.ai.task;

/** Represents the overall status of an {@link ITask} during its execution. */
public enum TaskStatus {
  /** The task is waiting to be executed. */
  PENDING,
  /** The task is still ongoing and requires further ticks. */
  RUNNING,
  /** The task has completed successfully. */
  SUCCESS,
  /** The task has failed to complete. */
  FAILURE,
  /** The task was cancelled because a new task was assigned. */
  CANCELLED_BY_NEW_TASK,
  /** The task was interrupted by an external factor (e.g., NPC fleeing). */
  INTERRUPTED,
  /** The task was interrupted specifically because the NPC started to flee. */
  INTERRUPTED_BY_FLEE
}
