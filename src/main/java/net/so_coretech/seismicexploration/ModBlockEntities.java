package net.so_coretech.seismicexploration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.so_coretech.seismicexploration.blockentity.BoomBoxBlockEntity;
import net.so_coretech.seismicexploration.blockentity.RecorderBlockEntity;
import net.so_coretech.seismicexploration.blockentity.SensorBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SeismicExploration.MODID);

    //
    // Register block entities
    //

    public static final Supplier<BlockEntityType<BoomBoxBlockEntity>> BOOM_BOX_ENTITY = BLOCK_ENTITIES.register(
            "boom_box_entity",
            () -> new BlockEntityType<>(BoomBoxBlockEntity::new, ModBlocks.BOOM_BOX.get()));

    public static final Supplier<BlockEntityType<SensorBlockEntity>> SENSOR_ENTITY = BLOCK_ENTITIES.register(
            "sensor_entity",
            () -> new BlockEntityType<>(SensorBlockEntity::new, ModBlocks.DFU.get()));

    public static final Supplier<BlockEntityType<RecorderBlockEntity>> RECORDER_ENTITY = BLOCK_ENTITIES.register(
            "recorder_entity",
            () -> new BlockEntityType<>(RecorderBlockEntity::new, ModBlocks.RECORDER.get()));

    //
    // Utilities
    //

    protected static void register(final IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
