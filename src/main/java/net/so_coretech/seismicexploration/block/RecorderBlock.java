package net.so_coretech.seismicexploration.block;

import com.mojang.logging.LogUtils;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.blockentity.RecorderBlockEntity;
import net.so_coretech.seismicexploration.blockentity.TickableBlockEntity;
import net.so_coretech.seismicexploration.network.RecorderPositionPacket;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class RecorderBlock extends HorizontalDirectionalBlock implements EntityBlock {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final MapCodec<RecorderBlock> CODEC = simpleCodec(RecorderBlock::new);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 4, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 12, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 12);
    private static final VoxelShape SHAPE_WEST = Block.box(4, 0, 0, 16, 16, 16);

    public RecorderBlock(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return ModBlockEntities.RECORDER_ENTITY.get().create(pos, state);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level,
                                  final BlockPos pos, final CollisionContext context) {
        final Direction dir = state.getValue(FACING);
        return switch (dir) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state,
                                          final Level level, final BlockPos pos, final Player player,
                                          final InteractionHand hand,
                                          final BlockHitResult hitResult) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (level.getBlockEntity(pos) instanceof final RecorderBlockEntity blockEntity) {
                if (!level.isClientSide()) {
                    // Start getting info recorded by sensors
                    blockEntity.powerOn();

                    // Send the block's position before opening the screen
                    sendBlockPositionToScreen((ServerPlayer) player, pos);

                    player.openMenu(blockEntity);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /**
     * Sends the given position to the client. This is needed to display the recorder's position in
     * the screen.
     *
     * @param pos the block's position to send.
     */
    private void sendBlockPositionToScreen(final ServerPlayer player, final BlockPos pos) {
        LOGGER.debug("sendBlockPositionToScreen({})", pos);
        PacketDistributor.sendToPlayer(player, new RecorderPositionPacket(pos));
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level,
                                                                            final BlockState state,
                                                                            final BlockEntityType<T> type) {
        return TickableBlockEntity.getTickerHelper(level);
    }
}
