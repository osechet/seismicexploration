package net.so_coretech.seismicexploration.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.so_coretech.seismicexploration.ModEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.entity.WorkerEntity;

@Mod.EventBusSubscriber(modid = SeismicExploration.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(final EntityAttributeCreationEvent event) {
        event.put(ModEntities.WORKER.get(), WorkerEntity.createAttributes().build());
    }
}
