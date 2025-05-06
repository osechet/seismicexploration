package net.so_code.seismicexploration.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class SourceBlock extends HorizontalDirectionalBlock {

    private static final MapCodec<SourceBlock> CODEC = simpleCodec(SourceBlock::new);

    public SourceBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.METAL).instabreak().sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}