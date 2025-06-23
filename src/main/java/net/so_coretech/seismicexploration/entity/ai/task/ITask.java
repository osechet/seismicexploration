package net.so_coretech.seismicexploration.entity.ai.task;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

/**
 * Represents a complex, player-assigned objective for an NPC. An ITask typically manages a sequence
 * or composition of {@link net.so_coretech.seismicexploration.entity.ai.action.IAction} instances
 * to achieve its goal.
 */
public interface ITask {

  /**
   * Called when the task is started. This is where initial setup for the task should occur,
   * including sending an "Order received" message to the ordering player.
   *
   * @param npc The NPC performing the task.
   * @param orderingPlayer The player who ordered the task, if any.
   */
  void start(WorkerEntity npc, @Nullable Player orderingPlayer);

  /**
   * Called every tick to update the task's state. This method is responsible for managing the
   * execution of underlying actions and determining the overall task status.
   *
   * @param npc The NPC performing the task.
   * @return The current status of the task (RUNNING, SUCCESS, or FAILURE).
   */
  TaskStatus tick(WorkerEntity npc);

  /**
   * Called when the task is stopped. This can be due to successful completion, failure,
   * cancellation, or interruption. An "Order complete/failed" message should be sent to the
   * ordering player if appropriate.
   *
   * @param npc The NPC performing the task.
   * @param status The final status of the task when it was stopped.
   */
  void stop(WorkerEntity npc, TaskStatus status);

  /**
   * Gets the {@link OrderType} associated with this task.
   *
   * @return The type of order this task represents.
   */
  OrderType getOrderType();

  /**
   * Called when the NPC performing this task takes damage. This allows the task to react to
   * threats, potentially pausing or altering its behavior.
   *
   * @param npc The NPC performing the task.
   * @param source The source of the damage.
   * @param amount The amount of damage taken.
   * @param orderingPlayer The player who ordered the task, if any.
   */
  void onDamaged(
      WorkerEntity npc, DamageSource source, float amount, @Nullable Player orderingPlayer);

  /**
   * Returns the current status of the task.
   *
   * @return the task's status.
   */
  TaskStatus getStatus();
}
