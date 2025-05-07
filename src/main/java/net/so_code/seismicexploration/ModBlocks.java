package net.so_code.seismicexploration;

import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.so_code.seismicexploration.block.BoomBoxBlock;
import net.so_code.seismicexploration.block.DFUBlock;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SeismicExploration.MODID);

    //
    // Register blocks
    //

    public static final RegistryObject<Block> BOOM_BOX_BLOCK = registerBlock("boom_box",
            () -> new BoomBoxBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("boom_box"))
                    .mapColor(MapColor.METAL).instabreak().sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY).noOcclusion()));

    public static final RegistryObject<Block> DFU_BLOCK = registerBlock("dfu",
            () -> new DFUBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("dfu"))
                    .mapColor(MapColor.METAL).instabreak().sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY).noOcclusion()));

    //
    // Utilities
    //

    private static <T extends Block> RegistryObject<T> registerBlock(String name,
            Supplier<T> block) {
        RegistryObject<T> ro = BLOCKS.register(name, block);
        registerBlockItem(name, ro);
        return ro;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.register(name,
                () -> new BlockItem(block.get(), new Item.Properties().setId(ModItems.key(name))));
    }

    protected static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
