package net.so_code.seismicexploration.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RecorderBlock extends HorizontalDirectionalBlock {

    private static final MapCodec<RecorderBlock> CODEC = simpleCodec(RecorderBlock::new);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 4, 16, 11, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 12, 11, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 11, 12);
    private static final VoxelShape SHAPE_WEST = Block.box(4, 0, 0, 16, 11, 16);

    public RecorderBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
            @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        Direction dir = state.getValue(FACING);
        switch (dir) {
            case EAST:
                return SHAPE_EAST;
            case SOUTH:
                return SHAPE_SOUTH;
            case WEST:
                return SHAPE_WEST;
            case NORTH:
            default:
                return SHAPE_NORTH;
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
