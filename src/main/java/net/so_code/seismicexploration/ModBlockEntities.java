package net.so_code.seismicexploration;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.so_code.seismicexploration.blockentity.BoomBoxBlockEntity;
import net.so_code.seismicexploration.blockentity.SensorBlockEntity;

public class ModBlockEntities {

        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.BLOCK_ENTITY_TYPES, SeismicExploration.MODID);

        //
        // Register block entities
        //

        public static final RegistryObject<BlockEntityType<BlockEntity>> BOOM_BOX_ENTITY =
                        register("boom_box_entity", BoomBoxBlockEntity::new,
                                        () -> Set.of(ModBlocks.BOOM_BOX.get()));

        public static final RegistryObject<BlockEntityType<BlockEntity>> SENSOR_ENTITY = register(
                        "sensor_entity", SensorBlockEntity::new, () -> Set.of(ModBlocks.DFU.get()));

        //
        // Utilities
        //

        private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(
                        String name, BlockEntityType.BlockEntitySupplier<T> factory,
                        Supplier<Set<Block>> validBlocks) {
                return BLOCK_ENTITIES.register(name,
                                () -> new BlockEntityType<>(factory, validBlocks.get()));
        }

        protected static void register(IEventBus eventBus) {
                BLOCK_ENTITIES.register(eventBus);
        }
}
