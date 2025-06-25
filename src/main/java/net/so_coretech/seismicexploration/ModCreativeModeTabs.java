package net.so_coretech.seismicexploration;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {

  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SeismicExploration.MODID);

  //
  // Register Creative Tab
  //

  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SEISMIC_TAB =
      CREATIVE_MODE_TABS.register(
          "seismic_blocks_tab",
          () ->
              CreativeModeTab.builder()
                  .icon(() -> new ItemStack(ModBlocks.DFU.get()))
                  .title(SeismicExploration.translatable("creativetab", "seismic_blocks_tab"))
                  .displayItems(
                      (parameters, output) -> {
                        output.accept(ModBlocks.DFU.get());
                        output.accept(ModBlocks.DFU3C.get());
                        output.accept(ModBlocks.AFU.get());
                        output.accept(ModBlocks.BOOM_BOX.get());
                        output.accept(ModBlocks.RECORDER.get());
                        output.accept(ModBlocks.CHARGE.get());
                        output.accept(ModItems.PETROLE.get());
                        output.accept(ModItems.MEMS.get());
                        output.accept(ModItems.PLASTIQUE.get());
                        output.accept(ModItems.GEOPHONE.get());
                        output.accept(ModItems.SUCRE_FERMENTE.get());
                      })
                  .build());

  //
  // Utilities
  //

  protected static void register(final IEventBus eventBus) {
    CREATIVE_MODE_TABS.register(eventBus);
  }
}
