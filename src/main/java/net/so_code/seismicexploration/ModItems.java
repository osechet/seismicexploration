package net.so_code.seismicexploration;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            SeismicExploration.MODID);

    //
    // Utilities
    //

    protected static ResourceKey<Item> key(String name) {
        return ModItems.ITEMS.key(name);
    }

    protected static <T extends Item> void register(String name, Supplier<T> item) {
        ITEMS.register(name, item);
    }

    protected static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
