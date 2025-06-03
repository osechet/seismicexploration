package net.so_coretech.seismicexploration.blockentity;

import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {

  void tick();

  @Nullable
  static <T extends BlockEntity> BlockEntityTicker<T> getTickerHelper(final Level level) {
    if (level.isClientSide()) {
      return null;
    }
    return (level0, pos, state, blockEntity) -> ((TickableBlockEntity) blockEntity).tick();
  }
}
