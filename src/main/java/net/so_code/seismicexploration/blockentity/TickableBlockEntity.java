package net.so_code.seismicexploration.blockentity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {

    void tick();

    static <T extends BlockEntity> BlockEntityTicker<T> getTickerHelper(final Level level) {
        return !level.isClientSide()
                ? (level0, pos, state, blockEntity) -> ((TickableBlockEntity) blockEntity).tick()
                : null;
    }
}
