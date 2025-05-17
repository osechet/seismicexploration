package net.so_coretech.seismicexploration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.so_coretech.seismicexploration.entity.WorkerEntity;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SeismicExploration.MODID);


    //
    // Register items
    //

    public static final RegistryObject<EntityType<WorkerEntity>> WORKER =
        register("worker", EntityType.Builder.of(WorkerEntity::new, MobCategory.MISC)
                                             .sized(0.6F, 1.95F));

    //
    // Utilities
    //

    private static ResourceKey<EntityType<?>> entityId(final String name) {
        return ENTITIES.key(name);
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(final String name,
                                                                             final EntityType.Builder<T> builder) {
        return ENTITIES.register(name, () -> builder.build(entityId(name)));
    }

    protected static void register(final IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
