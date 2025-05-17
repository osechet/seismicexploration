package net.so_coretech.seismicexploration.entity.ai.goal;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.so_coretech.seismicexploration.util.InventoryUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;

public class GetItemGoal extends Goal {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final PathfinderMob mob;
    private final IItemHandler inventory;
    private final Item item;
    private final int count;
    private final GoalListener listener;

    private enum Phase {CHECK_INVENTORY, FETCH_FROM_CONTAINER, DONE}

    private Phase phase = Phase.CHECK_INVENTORY;
    private @Nullable BlockPos nextPos;
    private @Nullable BlockEntity container;

    public GetItemGoal(final PathfinderMob mob, final IItemHandler inventory, final Item item, final int count,
                       final GoalListener listener) {
        this.mob = mob;
        this.inventory = inventory;
        this.item = item;
        this.count = count;
        this.listener = listener;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Always allow starting if not already done
        return phase != Phase.DONE;
    }

    @Override
    public void start() {
        phase = Phase.CHECK_INVENTORY;
    }

    @Override
    public void tick() {
        switch (phase) {
            case CHECK_INVENTORY -> handleCheckInventory();
            case FETCH_FROM_CONTAINER -> handleFetchFromContainer();
            case DONE -> mob.getNavigation().stop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return phase != Phase.DONE;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }


    private void handleCheckInventory() {
        final int inventoryCount = InventoryUtils.countItem(inventory, item);
        if (inventoryCount < this.count) {
            LOGGER.debug("Looking for {} items.", this.count - inventoryCount);
            final Optional<BlockEntity> blockEntity = InventoryUtils.findContainerWithSensor(
                mob.level(), mob.blockPosition(), 25, item, this.count - inventoryCount);
            if (blockEntity.isEmpty()) {
                LOGGER.debug("No item found in nearby chests. Aborting.");
                phase = Phase.DONE;
                nextPos = null;
                listener.onFailure("No item found in nearby chests. Aborting.");
            } else {
                LOGGER.debug("Going to chest to get more items.");
                phase = Phase.FETCH_FROM_CONTAINER;
                nextPos = blockEntity.get().getBlockPos();
                container = blockEntity.get();
            }
        } else {
            LOGGER.debug("Mob already has enough items in inventory.");
            phase = Phase.DONE;
            listener.onSucess();
        }
    }

    private void handleFetchFromContainer() {
        final double walkingSpeed = 1.0;
        if (nextPos != null && !mob.blockPosition().closerThan(nextPos, 1.2)) {
            LOGGER.debug("At {}, moving to container: {} ({} blocks away)",
                mob.blockPosition(), nextPos, mob.blockPosition().distSqr(nextPos));
            mob.getNavigation().moveTo(nextPos.getX(), nextPos.getY(), nextPos.getZ(), walkingSpeed);
        } else {
            if (container != null) {
                container.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    final int inventoryCount = InventoryUtils.countItem(inventory, item);
                    final int grabbed = InventoryUtils.moveItemsBetweenHandlers(
                        handler, inventory, item, this.count - inventoryCount);
                    LOGGER.debug("Grabbed {} items from chest", grabbed);
                });
                LOGGER.debug("Got the items.");
                phase = Phase.DONE;
                listener.onSucess();
            } else {
                LOGGER.warn("Container not available.");
                phase = Phase.DONE;
                listener.onFailure("Container not available.");
            }
        }
    }
}
