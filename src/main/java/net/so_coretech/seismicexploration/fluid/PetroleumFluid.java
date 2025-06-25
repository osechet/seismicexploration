package net.so_coretech.seismicexploration.fluid;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.so_coretech.seismicexploration.ModBlocks;
import net.so_coretech.seismicexploration.ModFluidTypes;
import net.so_coretech.seismicexploration.ModFluids;
import net.so_coretech.seismicexploration.ModItems;

public abstract class PetroleumFluid extends BaseFlowingFluid {

  public static final BaseFlowingFluid.Properties PROPERTIES =
      new BaseFlowingFluid.Properties(
              () -> ModFluidTypes.PETROLEUM_TYPE.get(),
              () -> ModFluids.PETROLEUM.get(),
              () -> ModFluids.FLOWING_PETROLEUM.get())
          .explosionResistance(100f)
          .bucket(() -> ModItems.PETROLEUM.get())
          .block(() -> (LiquidBlock) ModBlocks.PETROLEUM.get());

  private PetroleumFluid() {
    super(PROPERTIES);
  }

  @Override
  public ParticleOptions getDripParticle() {
    return ParticleTypes.ASH;
  }

  public static class Source extends PetroleumFluid {
    public int getAmount(FluidState state) {
      return 8;
    }

    public boolean isSource(FluidState state) {
      return true;
    }
  }

  public static class Flowing extends PetroleumFluid {
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    public int getAmount(FluidState state) {
      return state.getValue(LEVEL);
    }

    public boolean isSource(FluidState state) {
      return false;
    }
  }
}
