package net.so_coretech.seismicexploration.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Optional;

public class InventoryUtils {

    public static IItemHandler EMPTY = new ItemStackHandler(0);

    public static int countItem(final IItemHandler handler, final Item item) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            final ItemStack stack = handler.getStackInSlot(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countItem(final LazyOptional<IItemHandler> handler, final Item item) {
        if (handler.isPresent()) {
            return countItem(handler.orElse(EMPTY), item);
        } else {
            return 0;
        }
    }

    public static Optional<BlockEntity> findContainerWithSensor(final Level level, final BlockPos origin,
                                                                final int radius,
                                                                final Item item, final int wantCount) {
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) { // Search a bit vertically
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    final BlockEntity be = level.getBlockEntity(mutable);
                    if (be != null) {
                        final int itemCount = countItem(be.getCapability(ForgeCapabilities.ITEM_HANDLER), item);
                        if (itemCount >= wantCount) {
                            // Found enough items
                            return Optional.of(be);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static boolean takeSensorFromContainer(final Level level, final BlockPos containerPos, final Item item,
                                                  final int count) {
        final BlockEntity be = level.getBlockEntity(containerPos);
        if (be != null) {
            final var cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (cap.isPresent()) {
                final IItemHandler handler = cap.get();
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    final ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.is(item) && stack.getCount() > count) {
                        handler.extractItem(slot, count, false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static int moveItemsBetweenHandlers(final IItemHandler from, final IItemHandler to,
                                               final Item item, final int count) {
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

    public static int moveItemsBetweenHandlers(final LazyOptional<IItemHandler> from,
                                               final LazyOptional<IItemHandler> to,
                                               final Item item,
                                               final int count) {
        if (from.isPresent() && to.isPresent()) {
            return moveItemsBetweenHandlers(from.orElse(EMPTY),
                to.orElseThrow(() -> new IllegalStateException("Destination container must exist")), item, count);
        }
        return 0;
    }
}
