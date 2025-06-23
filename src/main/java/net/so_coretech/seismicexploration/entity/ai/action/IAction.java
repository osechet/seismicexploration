package net.so_coretech.seismicexploration.entity.ai.action;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.so_coretech.seismicexploration.entity.WorkerEntity;

/** Represents a single, atomic operation that an NPC can perform as part of a larger task. */
public interface IAction {

  /**
   * Checks if this action can fail without affecting the overall task status. This is useful for
   * executing next actions even if this one fails.
   *
   * @return true if the action can fail without impacting the task, false otherwise.
   */
  boolean allowFailure();

  /**
   * Called when the action is started.
   *
   * @param npc The NPC performing the action.
   * @param orderingPlayer The player who ordered the task, if any.
   */
  void start(WorkerEntity npc, @Nullable Player orderingPlayer);

  /**
   * Called every tick to update the action's state.
   *
   * @param npc The NPC performing the action.
   * @return The current status of the action (RUNNING, SUCCESS, or FAILURE).
   */
  ActionStatus tick(WorkerEntity npc);

  /**
   * Called when the action is stopped, either because it completed, failed, or was interrupted.
   *
   * @param npc The NPC performing the action.
   * @param status The final status of the action when it was stopped.
   */
  void stop(WorkerEntity npc, ActionStatus status);

  /**
   * Gets a human-readable name for this action, primarily for debugging purposes.
   *
   * @return The debug name of the action.
   */
  String getDebugName();
}
