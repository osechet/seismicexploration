package net.so_code.seismicexploration.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.so_code.seismicexploration.blockentity.RecorderBlockEntity;

public class RecorderBlock extends HorizontalDirectionalBlock implements EntityBlock {

    private static final MapCodec<RecorderBlock> CODEC = simpleCodec(RecorderBlock::new);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 4, 16, 11, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 12, 11, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 11, 12);
    private static final VoxelShape SHAPE_WEST = Block.box(4, 0, 0, 16, 11, 16);

    public RecorderBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull final BlockPos pos,
            @Nonnull final BlockState state) {
        return new RecorderBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(@Nonnull final BlockState state, @Nonnull final BlockGetter level,
            @Nonnull final BlockPos pos, @Nonnull final CollisionContext context) {
        final Direction dir = state.getValue(FACING);
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
    public BlockState getStateForPlacement(@Nonnull final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull final Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useItemOn(@Nonnull final ItemStack stack,
            @Nonnull final BlockState state, @Nonnull final Level level,
            @Nonnull final BlockPos pos, @Nonnull final Player player,
            @Nonnull final InteractionHand hand, @Nonnull final BlockHitResult hitResult) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (level.getBlockEntity(pos) instanceof final RecorderBlockEntity blockEntity) {
                if (!level.isClientSide()) {
                    ((ServerPlayer) player).openMenu(blockEntity);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
