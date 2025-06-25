/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.so_coretech.seismicexploration;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.so_coretech.seismicexploration.fluid.PetroleumFluid;

public class ModFluids {

  public static final DeferredRegister<Fluid> REGISTRY =
      DeferredRegister.create(BuiltInRegistries.FLUID, SeismicExploration.MODID);

  //
  // Register fluids
  //

  public static final DeferredHolder<Fluid, FlowingFluid> PETROLEUM =
      REGISTRY.register("petroleum", () -> new PetroleumFluid.Source());

  public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_PETROLEUM =
      REGISTRY.register("flowing_petroleum", () -> new PetroleumFluid.Flowing());

  //
  // Events
  //

  @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class FluidsClientSideHandler {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
      ItemBlockRenderTypes.setRenderLayer(PETROLEUM.get(), RenderType.translucent());
      ItemBlockRenderTypes.setRenderLayer(FLOWING_PETROLEUM.get(), RenderType.translucent());
    }
  }
}
