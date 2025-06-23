package net.so_coretech.seismicexploration.entity.ai.action;

/** Represents the status of an {@link IAction} during its execution. */
public enum ActionStatus {
  /** The action is still ongoing and requires further ticks. */
  RUNNING,
  /** The action has completed successfully. */
  SUCCESS,
  /** The action has failed to complete. */
  FAILURE
}
