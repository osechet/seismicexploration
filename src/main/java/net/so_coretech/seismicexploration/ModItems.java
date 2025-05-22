package net.so_coretech.seismicexploration;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;

public class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SeismicExploration.MODID);

    //
    // Register items
    //

    public static final DeferredItem<Item> BOOM_BOX =
            registerBlock(ModBlocks.BOOM_BOX, BlockItem::new, new Item.Properties());

    public static final DeferredItem<Item> DFU =
            registerBlock(ModBlocks.DFU, BlockItem::new, new Item.Properties());

    public static final DeferredItem<Item> RECORDER =
            registerBlock(ModBlocks.RECORDER, BlockItem::new, new Item.Properties());

    public static final DeferredItem<Item> FIELD_MONITOR =
            ITEMS.registerSimpleItem("field_monitor", new Item.Properties());

    //
    // Utilities
    //

    private static <T extends Block> DeferredItem<Item> registerBlock(
            final DeferredBlock<T> ro, final BiFunction<T, Item.Properties, Item> factory,
            final Item.Properties properties) {
        return ITEMS.registerItem(ro.getId().getPath(),
                (final Item.Properties props) -> factory.apply(ro.get(), props),
                properties.useBlockDescriptionPrefix());
    }

    protected static void register(final IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
