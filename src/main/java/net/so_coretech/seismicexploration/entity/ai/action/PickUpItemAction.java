package net.so_coretech.seismicexploration.entity.ai.action;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.util.InventoryUtils;
import org.slf4j.Logger;

public class PickUpItemAction implements IAction {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final Item itemToPickUp;
  private final int requiredCount;
  private final int searchRadius; // For finding containers
  private final boolean fetchFromContainer;

  private enum Phase {
    CHECK_OWN_INVENTORY,
    SEARCH_CONTAINER,
    MOVE_TO_CONTAINER,
    GRAB_FROM_CONTAINER,
    COMPLETED,
    FAILED
  }

  private Phase currentPhase;
  private @Nullable BlockPos containerPos;
  private @Nullable IAction subAction; // For MoveToPositionAction

  public PickUpItemAction(
      final Item itemToPickUp,
      final int requiredCount,
      final boolean fetchFromContainer,
      final int searchRadius) {
    this.itemToPickUp = itemToPickUp;
    this.requiredCount = requiredCount;
    this.fetchFromContainer = fetchFromContainer;
    this.searchRadius = searchRadius;
  }

  @Override
  public boolean allowFailure() {
    return false;
  }

  @Override
  public void start(final WorkerEntity npc, @Nullable final Player orderingPlayer) {
    LOGGER.debug(
        "NPC {} ({}) starting PickUpItemAction for {}x {}",
        npc.getId(),
        npc.getNickname(),
        requiredCount,
        itemToPickUp.getDescriptionId());
    this.currentPhase = Phase.CHECK_OWN_INVENTORY;
    this.containerPos = null;
    this.subAction = null;
  }

  @Override
  public ActionStatus tick(final WorkerEntity npc) {
    final IItemHandler npcInventory = npc.getCapability(Capabilities.ItemHandler.ENTITY);
    if (npcInventory == null) {
      LOGGER.warn(
          "NPC {} ({}) PickUpItemAction: NPC has no inventory capability.",
          npc.getId(),
          npc.getNickname());
      currentPhase = Phase.FAILED;
    }

    // If a sub-action (like moving) is active, tick it.
    if (subAction != null) {
      final ActionStatus subStatus = subAction.tick(npc);
      if (subStatus == ActionStatus.RUNNING) {
        return ActionStatus.RUNNING;
      }
      subAction.stop(npc, subStatus); // Stop the sub-action
      subAction = null; // Clear it

      if (subStatus == ActionStatus.FAILURE) {
        LOGGER.warn(
            "NPC {} ({}) PickUpItemAction: Sub-action {} failed. Phase: {}",
            npc.getId(),
            npc.getNickname(),
            (subAction != null ? subAction.getDebugName() : "unknown"),
            currentPhase);
        currentPhase = Phase.FAILED; // Mark main action as failed
      } else if (subStatus == ActionStatus.SUCCESS) {
        // Sub-action succeeded, proceed to next step in the current phase logic
        if (currentPhase == Phase.MOVE_TO_CONTAINER) {
          currentPhase = Phase.GRAB_FROM_CONTAINER;
        } else {
          // Unexpected success of sub-action, might be an error in state logic
          LOGGER.warn(
              "NPC {} ({}) PickUpItemAction: Sub-action succeeded in unexpected phase {}.",
              npc.getId(),
              npc.getNickname(),
              currentPhase);
          currentPhase = Phase.FAILED;
        }
      }
    }

    switch (currentPhase) {
      case CHECK_OWN_INVENTORY:
        return handleCheckOwnInventory(npc, npcInventory);
      case SEARCH_CONTAINER:
        return handleSearchContainer(npc);
      case MOVE_TO_CONTAINER:
        // This phase is now handled by the subAction logic above.
        // If subAction is null here, it means movement hasn't started or just finished.
        // If it just finished successfully, the subAction logic would have transitioned phase.
        // If it hasn't started, something is wrong.
        LOGGER.warn(
            "NPC {} ({}) PickUpItemAction: Reached MOVE_TO_CONTAINER phase without active subAction.",
            npc.getId(),
            npc.getNickname());
        currentPhase = Phase.FAILED; // Should not happen if logic is correct
        return ActionStatus.FAILURE;
      case GRAB_FROM_CONTAINER:
        return handleGrabFromContainer(npc, npcInventory);
      case COMPLETED:
        return ActionStatus.SUCCESS;
      case FAILED:
        return ActionStatus.FAILURE;
      default:
        LOGGER.error(
            "NPC {} ({}) PickUpItemAction: Reached unknown phase: {}",
            npc.getId(),
            npc.getNickname(),
            currentPhase);
        currentPhase = Phase.FAILED;
        return ActionStatus.FAILURE;
    }
  }

  private ActionStatus handleCheckOwnInventory(
      final WorkerEntity npc, final IItemHandler npcInventory) {
    if (npcInventory == null) return ActionStatus.FAILURE; // Should have been caught earlier

    final int currentItemCount = InventoryUtils.countItem(npcInventory, this.itemToPickUp);
    if (currentItemCount >= this.requiredCount) {
      LOGGER.debug(
          "NPC {} ({}) PickUpItemAction: Found enough {} in own inventory ({} >= {}).",
          npc.getId(),
          npc.getNickname(),
          itemToPickUp.getDescriptionId(),
          currentItemCount,
          requiredCount);
      currentPhase = Phase.COMPLETED;
      return ActionStatus.SUCCESS;
    }

    if (!this.fetchFromContainer) {
      LOGGER.debug(
          "NPC {} ({}) PickUpItemAction: Not enough {} in inventory and not allowed to fetch from containers. Required: {}, Has: {}.",
          npc.getId(),
          npc.getNickname(),
          itemToPickUp.getDescriptionId(),
          requiredCount,
          currentItemCount);
      currentPhase = Phase.FAILED;
      return ActionStatus.FAILURE;
    }

    LOGGER.debug(
        "NPC {} ({}) PickUpItemAction: Not enough {} in inventory (needs {}, has {}). Searching containers.",
        npc.getId(),
        npc.getNickname(),
        itemToPickUp.getDescriptionId(),
        requiredCount - currentItemCount,
        currentItemCount);
    currentPhase = Phase.SEARCH_CONTAINER;
    return ActionStatus.RUNNING; // Transition to next phase
  }

  private ActionStatus handleSearchContainer(final WorkerEntity npc) {
    final int itemsNeeded =
        this.requiredCount
            - InventoryUtils.countItem(
                npc.getCapability(Capabilities.ItemHandler.ENTITY), this.itemToPickUp);
    if (itemsNeeded <= 0) { // Double check, might have picked up items some other way
      currentPhase = Phase.COMPLETED;
      return ActionStatus.SUCCESS;
    }

    final Optional<BlockEntity> containerEntity =
        InventoryUtils.findContainerWithItem(
            npc.level(), npc.blockPosition(), searchRadius, itemToPickUp, itemsNeeded);

    if (containerEntity.isEmpty()) {
      LOGGER.debug(
          "NPC {} ({}) PickUpItemAction: No container found with enough {} within {} blocks.",
          npc.getId(),
          npc.getNickname(),
          itemToPickUp.getDescriptionId(),
          searchRadius);
      currentPhase = Phase.FAILED;
      return ActionStatus.FAILURE;
    }

    this.containerPos = containerEntity.get().getBlockPos();
    LOGGER.debug(
        "NPC {} ({}) PickUpItemAction: Found container with {} at {}. Moving to it.",
        npc.getId(),
        npc.getNickname(),
        itemToPickUp.getDescriptionId(),
        this.containerPos);
    this.subAction =
        new MoveToPositionAction(
            this.containerPos, 1.0D, 1.5F, false); // Standard speed, close acceptance
    this.subAction.start(npc, null); // No specific ordering player for this sub-action
    currentPhase = Phase.MOVE_TO_CONTAINER;
    return ActionStatus.RUNNING;
  }

  private ActionStatus handleGrabFromContainer(
      final WorkerEntity npc, final IItemHandler npcInventory) {
    if (this.containerPos == null || npcInventory == null) {
      LOGGER.warn(
          "NPC {} ({}) PickUpItemAction: containerPos or npcInventory is null in GRAB_FROM_CONTAINER phase.",
          npc.getId(),
          npc.getNickname());
      currentPhase = Phase.FAILED;
      return ActionStatus.FAILURE;
    }

    final IItemHandler containerHandler =
        npc.level().getCapability(Capabilities.ItemHandler.BLOCK, this.containerPos, null);
    if (containerHandler == null) {
      LOGGER.warn(
          "NPC {} ({}) PickUpItemAction: Could not get IItemHandler for container at {}.",
          npc.getId(),
          npc.getNickname(),
          this.containerPos);
      currentPhase = Phase.FAILED;
      return ActionStatus.FAILURE;
    }

    final int currentOwnCount = InventoryUtils.countItem(npcInventory, this.itemToPickUp);
    final int itemsStillNeeded = this.requiredCount - currentOwnCount;

    if (itemsStillNeeded <= 0) {
      LOGGER.debug(
          "NPC {} ({}) PickUpItemAction: Already have enough {} before grabbing from container.",
          npc.getId(),
          npc.getNickname(),
          itemToPickUp.getDescriptionId());
      currentPhase = Phase.COMPLETED;
      return ActionStatus.SUCCESS;
    }

    final int movedCount =
        InventoryUtils.moveItemsBetweenHandlers(
            containerHandler, npcInventory, this.itemToPickUp, itemsStillNeeded);
    LOGGER.debug(
        "NPC {} ({}) PickUpItemAction: Moved {}x {} from container at {} to NPC inventory.",
        npc.getId(),
        npc.getNickname(),
        movedCount,
        itemToPickUp.getDescriptionId(),
        this.containerPos);

    if (InventoryUtils.countItem(npcInventory, this.itemToPickUp) >= this.requiredCount) {
      currentPhase = Phase.COMPLETED;
      return ActionStatus.SUCCESS;
    } else {
      LOGGER.warn(
          "NPC {} ({}) PickUpItemAction: Still don't have enough {} after attempting to grab from container. Required: {}, Has: {}.",
          npc.getId(),
          npc.getNickname(),
          itemToPickUp.getDescriptionId(),
          requiredCount,
          InventoryUtils.countItem(npcInventory, this.itemToPickUp));
      currentPhase = Phase.FAILED; // Or could retry search if multiple containers are possible
      return ActionStatus.FAILURE;
    }
  }

  @Override
  public void stop(final WorkerEntity npc, final ActionStatus status) {
    LOGGER.debug(
        "NPC {} ({}) stopping PickUpItemAction for {} with status: {}",
        npc.getId(),
        npc.getNickname(),
        itemToPickUp.getDescriptionId(),
        status);
    if (subAction != null) {
      subAction.stop(npc, status); // Propagate stop to sub-action
      subAction = null;
    }
  }

  @Override
  public String getDebugName() {
    return "PickUpItemAction[item="
        + itemToPickUp.getDescriptionId()
        + ", count="
        + requiredCount
        + ", phase="
        + currentPhase
        + "]";
  }
}
