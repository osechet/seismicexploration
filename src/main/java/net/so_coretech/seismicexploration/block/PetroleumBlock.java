package net.so_coretech.seismicexploration.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.so_coretech.seismicexploration.ModFluids;

public class PetroleumBlock extends LiquidBlock {
  public PetroleumBlock(BlockBehaviour.Properties properties) {
    super(ModFluids.PETROLEUM.get(), properties);
  }
}
