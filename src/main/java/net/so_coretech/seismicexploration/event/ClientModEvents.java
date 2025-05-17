package net.so_coretech.seismicexploration.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.so_coretech.seismicexploration.ModEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.client.renderer.WorkerRenderer;

@Mod.EventBusSubscriber(modid = SeismicExploration.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WORKER.get(), WorkerRenderer::new);
    }
}
