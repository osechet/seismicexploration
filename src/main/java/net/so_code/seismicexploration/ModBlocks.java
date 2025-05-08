package net.so_code.seismicexploration;

import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.so_code.seismicexploration.block.BoomBoxBlock;
import net.so_code.seismicexploration.block.RecorderBlock;
import net.so_code.seismicexploration.block.SensorBlock;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SeismicExploration.MODID);

    //
    // Register blocks
    //

    public static final RegistryObject<Block> BOOM_BOX = registerBlock("boom_box",
            BoomBoxBlock::new, BlockBehaviour.Properties.of() // Properties:
                    .mapColor(MapColor.COLOR_BLUE) // the color on the map
                    .sound(SoundType.CROP) // the sound made when placed or destroyed
                    .noOcclusion() // avoid display issues with bigger surrounding blocks
    );

    public static final RegistryObject<Block> DFU = registerBlock("dfu", SensorBlock::new,
            BlockBehaviour.Properties.of() // Properties:
                    .mapColor(MapColor.COLOR_BLUE) // the color on the map
                    .sound(SoundType.CROP) // the sound made when placed or destroyed
                    .noOcclusion() // avoid display issues with bigger surrounding blocks
    );

    public static final RegistryObject<Block> RECORDER = registerBlock("recorder",
            RecorderBlock::new, BlockBehaviour.Properties.of() // Properties:
                    .mapColor(MapColor.METAL) // the color on the map
                    .sound(SoundType.STONE) // the sound made when placed or destroyed
                    .noOcclusion() // avoid display issues with bigger surrounding blocks
    );

    //
    // Utilities
    //

    private static ResourceKey<Block> blockId(String name) {
        return BLOCKS.key(name);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name,
            Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        RegistryObject<T> ro =
                BLOCKS.register(name, () -> factory.apply(properties.setId(blockId(name))));
        ModItems.registerBlock(ro, BlockItem::new, new Item.Properties());
        return ro;
    }

    protected static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
