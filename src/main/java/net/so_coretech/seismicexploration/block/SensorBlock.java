package net.so_coretech.seismicexploration.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.redstone.Orientation;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.blockentity.TickableBlockEntity;

public abstract class SensorBlock extends HorizontalDirectionalBlock implements EntityBlock {

  protected int radius;

  /**
   * Creates a new sensor block with the specified radius and properties. The radius defines how
   * many blocks around the sensor will be recorded. A radius of 0 means only one block column will
   * be recorded. A radius of 1 means a 3x3 area, radius of 2 means a 5x5 area, etc. Pay attention,
   * a higher radius will impact the performances.
   *
   * @param radius the radius of the recorded area.
   * @param properties
   */
  public SensorBlock(int radius, final BlockBehaviour.Properties properties) {
    super(properties);
    this.radius = radius;
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  public int getRadius() {
    return radius;
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
    return ModBlockEntities.SENSOR_ENTITY.get().create(pos, state);
  }

  @Override
  public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
    return this.defaultBlockState()
        .setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  protected void neighborChanged(
      BlockState state,
      Level level,
      BlockPos pos,
      Block neighborBlock,
      @Nullable Orientation orientation,
      boolean movedByPiston) {
    super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);

    // Check if the block *below* this block has changed
    if (!level.isClientSide) { // Server-side check
      BlockState stateBelow = level.getBlockState(pos.below());
      if (stateBelow.isAir() || stateBelow.canBeReplaced()) {
        // If the block below is air or replaceable, break this block
        level.destroyBlock(pos, true);
      }
    }
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final Level level, final BlockState state, final BlockEntityType<T> type) {
    return TickableBlockEntity.getTickerHelper(level);
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    BlockState below = level.getBlockState(pos.below());

    // Forbid to place a SensorBlock on top of another one
    return !(below.getBlock() instanceof SensorBlock);
  }
}
