package net.so_coretech.seismicexploration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModItems {

    private static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, SeismicExploration.MODID);

    //
    // Register items
    //

    public static final RegistryObject<Item> BOOM_BOX =
        registerBlock(ModBlocks.BOOM_BOX, BlockItem::new, new Item.Properties());

    public static final RegistryObject<Item> DFU =
        registerBlock(ModBlocks.DFU, BlockItem::new, new Item.Properties());

    public static final RegistryObject<Item> RECORDER =
        registerBlock(ModBlocks.RECORDER, BlockItem::new, new Item.Properties());

    public static final RegistryObject<Item> FIELD_MONITOR =
        registerItem("field_monitor", Item::new, new Item.Properties());

    //
    // Utilities
    //

    private static ResourceKey<Item> itemId(final String name) {
        return ITEMS.key(name);
    }

    private static <T extends Block> ResourceKey<Item> blockIdToItemId(
        final ResourceKey<T> blockId) {
        return ResourceKey.create(ITEMS.getRegistryKey(), blockId.location());
    }

    private static <T extends Item> RegistryObject<T> registerItem(final String name,
                                                                   final Function<Item.Properties, T> factory,
                                                                   final Item.Properties properties) {
        return ITEMS.register(name, () -> factory.apply(properties.setId(itemId(name))));
    }

    private static <T extends Block> RegistryObject<Item> registerBlock(
        final RegistryObject<T> ro, final BiFunction<T, Item.Properties, Item> factory,
        final Item.Properties properties) {
        return registerItem(blockIdToItemId(Objects.requireNonNull(ro.getKey())).location().getPath(),
            (final Item.Properties props) -> factory.apply(ro.get(), props),
            properties.useBlockDescriptionPrefix());
    }

    protected static void register(final IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
