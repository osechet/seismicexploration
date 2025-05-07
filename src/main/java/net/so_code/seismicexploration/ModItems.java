package net.so_code.seismicexploration;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

public class ModItems {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SeismicExploration.MODID);

    //
    // Register items
    //

    public static final RegistryObject<Item> FIELD_MONITOR =
            registerItem("field_monitor", Item::new, new Item.Properties());

    //
    // Utilities
    //

    private static ResourceKey<Item> itemId(String name) {
        return ITEMS.key(name);
    }

    private static <T extends Block> ResourceKey<Item> blockIdToItemId(ResourceKey<T> blockId) {
        return ResourceKey.create(ITEMS.getRegistryKey(), blockId.location());
    }

    private static <T extends Item> RegistryObject<T> registerItem(String name,
            Function<Item.Properties, T> ctor, Item.Properties properties) {
        RegistryObject<T> ro =
                ITEMS.register(name, () -> ctor.apply(properties.setId(itemId(name))));
        return ro;
    }

    protected static <T extends Block> RegistryObject<Item> registerBlock(RegistryObject<T> ro,
            BiFunction<T, Item.Properties, Item> ctor, Item.Properties properties) {
        return registerItem(blockIdToItemId(ro.getKey()).location().getPath(),
                (Item.Properties props) -> ctor.apply(ro.get(), props),
                properties.useBlockDescriptionPrefix());
    }

    protected static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
