/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.so_coretech.seismicexploration;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.so_coretech.seismicexploration.fluid.types.PetroleFluidType;

public class ModFluidTypes {
  public static final DeferredRegister<FluidType> REGISTRY =
      DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, SeismicExploration.MODID);
  public static final DeferredHolder<FluidType, FluidType> PETROLE_TYPE =
      REGISTRY.register("petrole", () -> new PetroleFluidType());
}
