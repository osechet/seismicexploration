package net.so_coretech.seismicexploration.entity.ai.action;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public class PlaceItemAction implements IAction {

  private static final Logger LOGGER = LogUtils.getLogger();

  private final Item itemToPlace;
  private final BlockPos targetPos;
  private @Nullable BlockPlacedListener blockPlacedListener;

  public PlaceItemAction(final Item itemToPlace, final BlockPos targetPos) {
    this.itemToPlace = itemToPlace;
    this.targetPos = targetPos;
  }

  public void setBlockPlacedListener(@Nullable final BlockPlacedListener blockPlacedListener) {
    this.blockPlacedListener = blockPlacedListener;
  }

  @Override
  public boolean allowFailure() {
    return false;
  }

  @Override
  public void start(final WorkerEntity npc, @Nullable final Player orderingPlayer) {
    LOGGER.debug(
        "NPC {} ({}) starting PlaceItemAction, item: {}, target: {}",
        npc.getId(),
        npc.getNickname(),
        itemToPlace.getDescriptionId(),
        targetPos);
    // No initial setup needed beyond what tick handles
  }

  @Override
  public ActionStatus tick(final WorkerEntity npc) {
    if (npc.level().isClientSide) {
      return ActionStatus.RUNNING; // Should not happen if called from server-side task
    }

    final IItemHandler npcInventory = npc.getCapability(Capabilities.ItemHandler.ENTITY);
    if (npcInventory == null) {
      LOGGER.warn(
          "NPC {} ({}) PlaceItemAction: NPC has no inventory capability.",
          npc.getId(),
          npc.getNickname());
      return ActionStatus.FAILURE;
    }

    int itemSlot = -1;
    ItemStack itemStackToPlace = ItemStack.EMPTY;

    for (int i = 0; i < npcInventory.getSlots(); i++) {
      final ItemStack stackInSlot = npcInventory.getStackInSlot(i);
      if (stackInSlot.is(this.itemToPlace) && !stackInSlot.isEmpty()) {
        itemSlot = i;
        itemStackToPlace = stackInSlot;
        break;
      }
    }

    if (itemSlot == -1 || itemStackToPlace.isEmpty()) {
      LOGGER.warn(
          "NPC {} ({}) PlaceItemAction: Could not find item {} in inventory.",
          npc.getId(),
          npc.getNickname(),
          this.itemToPlace.getDescriptionId());
      return ActionStatus.FAILURE;
    }

    // Ensure the target position is replaceable
    final Level level = npc.level();
    if (!level.getBlockState(this.targetPos).canBeReplaced()) {
      // Check if the block is already the one we want to place
      if (this.itemToPlace instanceof BlockItem blockItem) {
        if (level.getBlockState(this.targetPos).is(blockItem.getBlock())) {
          LOGGER.debug(
              "NPC {} ({}) PlaceItemAction: Block {} already present at {}. Action success.",
              npc.getId(),
              npc.getNickname(),
              blockItem.getBlock().getDescriptionId(),
              this.targetPos);
          return ActionStatus.SUCCESS; // Already placed
        }
      }
      LOGGER.warn(
          "NPC {} ({}) PlaceItemAction: Target position {} is not replaceable and not the target block.",
          npc.getId(),
          npc.getNickname(),
          this.targetPos);
      return ActionStatus.FAILURE;
    }

    if (this.itemToPlace instanceof final BlockItem blockItem) {
      // Simulate player placing the block for context
      // This might need adjustment based on specific block placement requirements
      final BlockPlaceContext placeContext =
          new BlockPlaceContext(
              level,
              null, // No player, NPC is placing
              InteractionHand.MAIN_HAND,
              itemStackToPlace,
              new BlockHitResult(
                  Vec3.atCenterOf(this.targetPos.below()), // Hit from below center
                  net.minecraft.core.Direction.UP, // Placing on top face
                  this.targetPos,
                  false));

      final BlockState blockStateToPlace = blockItem.getBlock().getStateForPlacement(placeContext);
      if (blockStateToPlace == null) {
        LOGGER.warn(
            "NPC {} ({}) PlaceItemAction: Could not get BlockState for placement for item {}.",
            npc.getId(),
            npc.getNickname(),
            this.itemToPlace.getDescriptionId());
        return ActionStatus.FAILURE;
      }

      if (level.setBlock(this.targetPos, blockStateToPlace, 3)) {
        LOGGER.debug(
            "NPC {} ({}) PlaceItemAction: Successfully placed {} at {}.",
            npc.getId(),
            npc.getNickname(),
            this.itemToPlace.getDescriptionId(),
            this.targetPos);
        npcInventory.extractItem(itemSlot, 1, false); // Consume item

        if (blockPlacedListener != null) {
          blockPlacedListener.onBlockPlaced((ServerLevel) level, this.targetPos);
        }

        return ActionStatus.SUCCESS;
      } else {
        LOGGER.warn(
            "NPC {} ({}) PlaceItemAction: level.setBlock returned false for {} at {}.",
            npc.getId(),
            npc.getNickname(),
            this.itemToPlace.getDescriptionId(),
            this.targetPos);
        return ActionStatus.FAILURE;
      }
    } else {
      // Item is not a BlockItem, cannot be "placed" in this manner.
      // This action might need to be more generic or have different logic for non-block items.
      LOGGER.warn(
          "NPC {} ({}) PlaceItemAction: Item {} is not a BlockItem. Cannot place.",
          npc.getId(),
          npc.getNickname(),
          this.itemToPlace.getDescriptionId());
      return ActionStatus.FAILURE;
    }
  }

  @Override
  public void stop(final WorkerEntity npc, final ActionStatus status) {
    LOGGER.debug(
        "NPC {} ({}) stopping PlaceItemAction for item {} at {} with status: {}",
        npc.getId(),
        npc.getNickname(),
        itemToPlace.getDescriptionId(),
        targetPos,
        status);
    // No specific cleanup needed
  }

  @Override
  public String getDebugName() {
    return "PlaceItemAction[item=" + itemToPlace.getDescriptionId() + ", pos=" + targetPos + "]";
  }

  public static interface BlockPlacedListener {
    void onBlockPlaced(ServerLevel serverLevel, BlockPos pos);
  }
}
