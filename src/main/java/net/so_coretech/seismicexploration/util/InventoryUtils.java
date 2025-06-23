package net.so_coretech.seismicexploration.util;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class InventoryUtils {

  public static int countItem(@Nullable final IItemHandler handler, final Item item) {
    if (handler == null) {
      return 0;
    }
    int count = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      final ItemStack stack = handler.getStackInSlot(i);
      if (stack.is(item)) {
        count += stack.getCount();
      }
    }
    return count;
  }

  public static Optional<BlockEntity> findContainerWithItem(
      final Level level,
      final BlockPos origin,
      final int radius,
      final Item item,
      final int wantCount) {
    final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = -2; dy <= 2; dy++) { // Search a bit vertically
        for (int dz = -radius; dz <= radius; dz++) {
          mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
          final BlockEntity be = level.getBlockEntity(mutable);
          if (be != null) {
            final IItemHandler handler =
                level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
            if (countItem(handler, item) >= wantCount) {
              // Found enough items
              return Optional.of(be);
            }
          }
        }
      }
    }
    return Optional.empty();
  }

  public static int moveItemsBetweenHandlers(
      @Nullable final IItemHandler from, final IItemHandler to, final Item item, final int count) {
    if (from == null) {
      return 0;
    }

    int moved = 0;
    for (int slot = 0; slot < from.getSlots() && moved < count; slot++) {
      final ItemStack stackInSlot = from.getStackInSlot(slot);
      if (stackInSlot.is(item) && !stackInSlot.isEmpty()) {
        final int toExtract = Math.min(stackInSlot.getCount(), count - moved);
        ItemStack extracted = from.extractItem(slot, toExtract, false);
        if (!extracted.isEmpty()) {
          // Try to insert into destination, merging stacks if possible
          for (int toSlot = 0; toSlot < to.getSlots() && !extracted.isEmpty(); toSlot++) {
            extracted = to.insertItem(toSlot, extracted, false);
          }
          final int actuallyMoved = toExtract - extracted.getCount();
          moved += actuallyMoved;
          // If some items couldn't be inserted, put them back
          if (!extracted.isEmpty()) {
            from.insertItem(slot, extracted, false);
          }
        }
      }
    }
    return moved;
  }
}
