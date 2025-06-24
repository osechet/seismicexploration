package net.so_coretech.seismicexploration.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.blockentity.TickableBlockEntity;

public class SensorBlock extends HorizontalDirectionalBlock implements EntityBlock {

  private static final MapCodec<SensorBlock> CODEC = simpleCodec(SensorBlock::new);
  private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 4, 10);

  public SensorBlock(final BlockBehaviour.Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
    return ModBlockEntities.SENSOR_ENTITY.get().create(pos, state);
  }

  @Override
  protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
    return CODEC;
  }

  @Override
  public VoxelShape getShape(
      final BlockState state,
      final BlockGetter level,
      final BlockPos pos,
      final CollisionContext context) {
    return SHAPE;
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

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final Level level, final BlockState state, final BlockEntityType<T> type) {
    return TickableBlockEntity.getTickerHelper(level);
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    BlockState below = level.getBlockState(pos.below());

    // Interdire de poser un SensorBlock sur un autre SensorBlock
    return !(below.getBlock() instanceof SensorBlock);
  }

  /*@Override
  protected InteractionResult useItemOn(
      final ItemStack stack,
      final BlockState state,
      final Level level,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult hitResult) {
    if (hand == InteractionHand.MAIN_HAND) {
      if (!level.isClientSide()) {
        // Interdire de poser un block sur un SensorBlock
        return InteractionResult.CONSUME;
      }
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
  }*/
}
