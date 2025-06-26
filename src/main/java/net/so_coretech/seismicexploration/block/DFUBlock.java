package net.so_coretech.seismicexploration.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DFUBlock extends SensorBlock {

  private static final MapCodec<DFUBlock> CODEC = simpleCodec(DFUBlock::new);
  private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 4, 10);

  public DFUBlock(final BlockBehaviour.Properties properties) {
    super(1, Math.toRadians(45), properties);
  }

  @Override
  protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getShape(
      final BlockState state,
      final BlockGetter level,
      final BlockPos pos,
      final CollisionContext context) {
    return SHAPE;
  }
}
