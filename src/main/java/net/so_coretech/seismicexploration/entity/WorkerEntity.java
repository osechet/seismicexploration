package net.so_coretech.seismicexploration.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.so_coretech.seismicexploration.entity.ai.goal.ExecuteComplexTaskGoal;
import net.so_coretech.seismicexploration.entity.ai.goal.FleeWhenAttackedGoal;
import net.so_coretech.seismicexploration.entity.ai.task.ITask;
import net.so_coretech.seismicexploration.entity.ai.task.TaskStatus;
import net.so_coretech.seismicexploration.network.OpenWorkerOrderMenuPacket;

public class WorkerEntity extends PathfinderMob {

  private static final String[] NAMES = {
    "Alan", "Bob", "Charlie", "Dave", "Edward", "Frank", "Garry", "Hector", "Igor", "Jack",
    "Kevin", "Liam", "Mike", "Nate", "Oscar", "Paul", "Quentin", "Rick", "Sam", "Tom",
    "Ulysses", "Victor", "Winston", "Xander", "Yuri", "Zane"
  };
  private final ItemStackHandler inventory;
  private boolean frozen;
  private float lastDamageTaken;

  private @Nullable ITask currentTask;
  private @Nullable Player taskOrderingPlayer;

  public WorkerEntity(final EntityType<? extends PathfinderMob> type, final Level level) {
    super(type, level);
    String nickname = NAMES[this.random.nextInt(NAMES.length)];
    this.setCustomName(
        Component.translatable("message.seismicexploration.worker_custom_name", nickname));
    this.setCustomNameVisible(true);
    this.inventory = new ItemStackHandler(9);

    // Prevent swimming completely by setting it to a negative value (unwalkable)
    setPathfindingMalus(PathType.WATER, -1.0F);
    setPathfindingMalus(PathType.WATER_BORDER, 100.0F); // Set a high penalty for water edges
  }

  public String getNickname() {
    return this.getCustomName() != null ? this.getCustomName().getString() : "Unnamed Worker";
  }

  public IItemHandler getInventory() {
    return inventory;
  }

  public void setFrozen(final boolean frozen) {
    this.frozen = frozen;
  }

  /**
   * Assigns a new task to this worker. If a task is already running, it will be stopped with {@link
   * TaskStatus#CANCELLED_BY_NEW_TASK}. The new task will then be started.
   *
   * @param newTask The new task to assign.
   * @param orderingPlayer The player who ordered this task, if any.
   */
  public void assignTask(final ITask newTask, @Nullable final Player orderingPlayer) {
    if (this.currentTask != null && this.currentTask.getStatus() != TaskStatus.SUCCESS) {
      this.currentTask.stop(this, TaskStatus.CANCELLED_BY_NEW_TASK);
    }
    this.currentTask = newTask;
    this.taskOrderingPlayer = orderingPlayer;
    if (this.currentTask != null) {
      this.currentTask.start(this, this.taskOrderingPlayer);
    }
  }

  @Nullable
  public ITask getCurrentTask() {
    return currentTask;
  }

  @Nullable
  public Player getTaskOrderingPlayer() {
    return taskOrderingPlayer;
  }

  public float getLastDamageTaken() {
    return lastDamageTaken;
  }

  @Override
  protected void actuallyHurt(
      final ServerLevel level, final DamageSource damageSrc, final float damageAmount) {
    // This method is called by LivingEntity.hurt after damage reduction (armor, effects)
    // and invulnerability checks. damageAmount is the final amount of damage to be applied.
    super.actuallyHurt(level, damageSrc, damageAmount); // Apply the damage

    // Now that damage has been applied, record it and notify tasks.
    if (this.isAlive()) { // Check if still alive after damage
      this.lastDamageTaken = damageAmount; // Store the actual damage amount
      if (this.currentTask != null) {
        this.currentTask.onDamaged(this, damageSrc, damageAmount, this.taskOrderingPlayer);
      }
    }
  }

  @Override
  public void readAdditionalSaveData(final CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.contains("Inventory")) {
      final var provider = this.level().registryAccess();
      inventory.deserializeNBT(provider, tag.getCompoundOrEmpty("Inventory"));
    }
  }

  @Override
  public void addAdditionalSaveData(final CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    final var provider = this.level().registryAccess();
    tag.put("Inventory", inventory.serializeNBT(provider));
  }

  @Override
  public void aiStep() {
    if (!frozen) {
      super.aiStep();
    }
  }

  @Override
  protected void registerGoals() {
    // Highest priority: Flee if attacked and an ordering player is known
    this.goalSelector.addGoal(0, new FleeWhenAttackedGoal(this));
    // Next priority: Execute the currently assigned complex task
    this.goalSelector.addGoal(1, new ExecuteComplexTaskGoal(this));
    // Default low-priority behavior: Wander around
    this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0D));
  }

  @Override
  public InteractionResult interactAt(
      final Player player, final Vec3 vec, final InteractionHand hand) {
    if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
      // Make the NPC face the player
      this.lookAt(player, 360.0F, 360.0F);
      // Sync body rotation to head rotation for instant full turn
      this.yBodyRot = this.getYHeadRot();
      this.setYRot(this.getYHeadRot());
      // Make the NPC stop moving
      setFrozen(true);
      // Open the worker order menu
      PacketDistributor.sendToPlayer(
          (ServerPlayer) player,
          new OpenWorkerOrderMenuPacket(this.getId(), player.blockPosition()));
      return InteractionResult.SUCCESS;
    }
    return super.interactAt(player, vec, hand);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return PathfinderMob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 20.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.25D);
  }
}
