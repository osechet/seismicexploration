package net.so_coretech.seismicexploration.fluid.types;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.so_coretech.seismicexploration.ModFluidTypes;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class PetroleumFluidType extends FluidType {
  public PetroleumFluidType() {
    super(
        FluidType.Properties.create()
            .canSwim(false)
            .canDrown(false)
            .pathType(PathType.LAVA)
            .adjacentPathType(null)
            .motionScale(0.007D)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH));
  }

  @SubscribeEvent
  public static void registerFluidTypeExtensions(RegisterClientExtensionsEvent event) {
    event.registerFluidType(
        new IClientFluidTypeExtensions() {
          private static final ResourceLocation STILL_TEXTURE =
              ResourceLocation.parse("seismicexploration:block/petroleum_flowing");
          private static final ResourceLocation FLOWING_TEXTURE =
              ResourceLocation.parse("seismicexploration:block/still_petroleum_16x16");

          @Override
          public ResourceLocation getStillTexture() {
            return STILL_TEXTURE;
          }

          @Override
          public ResourceLocation getFlowingTexture() {
            return FLOWING_TEXTURE;
          }

          @Override
          public FogParameters modifyFogRender(
              Camera camera,
              FogRenderer.FogMode mode,
              float renderDistance,
              float partialTick,
              FogParameters fogParameters) {
            float nearDistance = fogParameters.start();
            float farDistance = fogParameters.end();
            Entity entity = camera.getEntity();
            Level world = entity.level();
            return new FogParameters(
                0f,
                Math.min(48f, renderDistance),
                FogShape.SPHERE,
                fogParameters.red(),
                fogParameters.green(),
                fogParameters.blue(),
                fogParameters.alpha());
          }
        },
        ModFluidTypes.PETROLEUM_TYPE.get());
  }
}
