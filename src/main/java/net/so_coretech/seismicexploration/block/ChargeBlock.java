package net.so_coretech.seismicexploration.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.ModItems;
import net.so_coretech.seismicexploration.blockentity.ChargeBlockEntity;
import net.so_coretech.seismicexploration.blockentity.TickableBlockEntity;

public class ChargeBlock extends Block implements EntityBlock {

  public static final BooleanProperty PRIMED = BooleanProperty.create("primed");
  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  private static final MapCodec<ChargeBlock> CODEC = simpleCodec(ChargeBlock::new);
  private static final VoxelShape SHAPE = Block.box(7, 0, 7, 9, 10, 9);

  public ChargeBlock(final BlockBehaviour.Properties properties) {
    super(properties.lightLevel(state -> state.getValue(LIT) ? 15 : 0));
    this.registerDefaultState(
        this.stateDefinition.any().setValue(PRIMED, false).setValue(LIT, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(PRIMED).add(LIT);
  }

  @Override
  public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
    return ModBlockEntities.CHARGE_ENTITY.get().create(pos, state);
  }

  @Override
  protected MapCodec<? extends Block> codec() {
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

  @Override
  protected InteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hitResult) {
    if (!level.isClientSide) {
      if (!state.getValue(PRIMED)) {
        ItemStack heldItem = player.getItemInHand(hand);
        // Check if the player is holding a Blaster
        if (heldItem.is(ModItems.BLASTER.get().asItem())) {

          BlockEntity be = level.getBlockEntity(pos);
          if (be instanceof ChargeBlockEntity chargeEntity) {
            chargeEntity.detonate();
          }

          return InteractionResult.CONSUME;
        }
      }
    }
    return InteractionResult.PASS;
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final Level level, final BlockState state, final BlockEntityType<T> type) {
    return TickableBlockEntity.getTickerHelper(level);
  }
}
