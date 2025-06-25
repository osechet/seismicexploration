package net.so_coretech.seismicexploration.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.so_coretech.seismicexploration.ModFluids;

public class PetroleumBlock extends LiquidBlock {
  public PetroleumBlock(BlockBehaviour.Properties properties) {
    super(
        ModFluids.PETROLEUM.get(),
        properties
            .mapColor(MapColor.COLOR_BLACK)
            .strength(100f)
            .noCollission()
            .noLootTable()
            .liquid()
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.EMPTY)
            .replaceable());
  }
}
