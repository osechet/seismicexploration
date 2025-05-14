package net.so_coretech.seismicexploration.blockentity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

import javax.annotation.Nullable;

public interface TickableBlockEntity {

    void tick();

    static <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTickerHelper(final Level level) {
        if (level.isClientSide()) {
            return null;
        }
        return (level0, pos, state, blockEntity) -> ((TickableBlockEntity) blockEntity).tick();
    }
}
