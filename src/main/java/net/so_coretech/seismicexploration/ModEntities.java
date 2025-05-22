package net.so_coretech.seismicexploration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.so_coretech.seismicexploration.entity.WorkerEntity;

import java.util.function.Supplier;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.createEntities(SeismicExploration.MODID);


    //
    // Register items
    //

    public static final Supplier<EntityType<WorkerEntity>> WORKER =
            register("worker", EntityType.Builder.of(WorkerEntity::new, MobCategory.MISC)
                                                 .sized(0.6F, 1.95F));

    //
    // Utilities
    //

    private static ResourceKey<EntityType<?>> entityId(final String name) {
        return ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, name)
        );
    }

    private static <T extends Entity> Supplier<EntityType<T>> register(final String name,
                                                                       final EntityType.Builder<T> builder) {
        return ENTITIES.register(name, () -> builder.build(entityId(name)));
    }

    protected static void register(final IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
