package net.so_code.seismicexploration.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.so_code.seismicexploration.ModBlockEntities;
import net.so_code.seismicexploration.blockentity.BoomBoxBlockEntity;
import net.so_code.seismicexploration.blockentity.TickableBlockEntity;

public class BoomBoxBlock extends HorizontalDirectionalBlock implements EntityBlock {

    private static final MapCodec<BoomBoxBlock> CODEC = simpleCodec(BoomBoxBlock::new);
    private static final VoxelShape SHAPE_NORTH = Block.box(3, 0, 7, 13, 12, 10);
    private static final VoxelShape SHAPE_EAST = Block.box(6, 0, 3, 9, 12, 13);
    private static final VoxelShape SHAPE_SOUTH = Block.box(3, 0, 6, 13, 12, 9);
    private static final VoxelShape SHAPE_WEST = Block.box(7, 0, 3, 10, 12, 13);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public BoomBoxBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false).setValue(WORKING, false));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return ModBlockEntities.BOOM_BOX_ENTITY.get().create(pos, state);
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
        builder.add(FACING, POWERED, WORKING);
    }

    @Override
    protected InteractionResult useItemOn(@Nonnull ItemStack stack, @Nonnull BlockState state,
            @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player,
            @Nonnull InteractionHand hand, @Nonnull BlockHitResult hitResult) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BoomBoxBlockEntity blockEntity) {
                blockEntity.switchPower();
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
            @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return TickableBlockEntity.getTickerHelper(level);
    }
}
