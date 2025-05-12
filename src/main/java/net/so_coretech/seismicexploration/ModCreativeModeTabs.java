package net.so_coretech.seismicexploration;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SeismicExploration.MODID);

    //
    // Register Creative Tab
    //

    public static final RegistryObject<CreativeModeTab> SEISMIC_TAB = CREATIVE_MODE_TABS.register(
            "seismic_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DFU.get()))
                    .title(SeismicExploration.translatable("creativetab", "seismic_blocks_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.DFU.get());
                        output.accept(ModBlocks.BOOM_BOX.get());
                        output.accept(ModBlocks.RECORDER.get());
                        output.accept(ModItems.FIELD_MONITOR.get());
                    }).build());

    //
    // Utilities
    //

    protected static void register(final IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
