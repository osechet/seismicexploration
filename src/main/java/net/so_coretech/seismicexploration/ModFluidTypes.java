/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.so_coretech.seismicexploration;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.so_coretech.seismicexploration.fluid.types.PetroleumFluidType;

public class ModFluidTypes {

  public static final DeferredRegister<FluidType> REGISTRY =
      DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, SeismicExploration.MODID);

  //
  // Register fluid types
  //

  public static final DeferredHolder<FluidType, FluidType> PETROLEUM_TYPE =
      REGISTRY.register("petroleum", () -> new PetroleumFluidType());
}
