package net.so_coretech.seismicexploration;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.so_coretech.seismicexploration.menu.RecorderMenu;

public class ModMenus {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SeismicExploration.MODID);

    //
    // Register menu types
    //

    public static final RegistryObject<MenuType<RecorderMenu>> RECORDER_MENU =
            register("recorder_menu", RecorderMenu::new);

    //
    // Utilities
    //

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(
            final String name, final MenuType.MenuSupplier<T> factory) {
        return MENUS.register(name, () -> new MenuType<>(factory, FeatureFlags.VANILLA_SET));
    }

    protected static void register(final IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
