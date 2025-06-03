package net.so_coretech.seismicexploration;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.so_coretech.seismicexploration.client.renderer.WorkerRenderer;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.screen.RecorderScreen;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SeismicExploration.MODID)
public class SeismicExploration {
  // Define mod id in a common place for everything to reference
  public static final String MODID = "seismicexploration";
  // Directly reference a slf4j logger
  private static final Logger LOGGER = LogUtils.getLogger();

  public SeismicExploration(final IEventBus modEventBus, final ModContainer modContainer) {
    // Register the commonSetup method for modloading
    modEventBus.addListener(this::commonSetup);

    // Register the blocks and items to the mod event bus
    ModBlockEntities.register(modEventBus);
    ModBlocks.register(modEventBus);
    ModCreativeModeTabs.register(modEventBus);
    ModEntities.register(modEventBus);
    ModItems.register(modEventBus);
    ModMenus.register(modEventBus);

    // Register ourselves for server and other game events we are interested in
    NeoForge.EVENT_BUS.register(this);

    //        // Register the item to a creative tab
    //        modEventBus.addListener(this::addCreative);

    modEventBus.addListener(this::registerCapabilities);

    // Register our mod's ForgeConfigSpec so that Forge can create and load the
    // config file for us
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    // Some common setup code
    LOGGER.info("HELLO FROM COMMON SETUP");

    if (Config.logDirtBlock) {
      LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
    }

    LOGGER.info("{} {}", Config.magicNumberIntroduction, Config.magicNumber);

    Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
  }

  //    // Add the example block item to the building blocks tab
  //    private void addCreative(BuildCreativeModeTabContentsEvent event)
  //    {
  //        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
  //            event.accept(EXAMPLE_BLOCK_ITEM);
  //    }

  private void registerCapabilities(final RegisterCapabilitiesEvent event) {
    event.registerEntity(
        Capabilities.ItemHandler.ENTITY,
        ModEntities.WORKER.get(),
        (entity, context) -> entity.getInventory());
  }

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  public void onServerStarting(final ServerStartingEvent event) {
    // Do something when the server starts
    LOGGER.info("HELLO from server starting");
  }

  // You can use EventBusSubscriber to automatically register all static methods
  // in the class annotated with @SubscribeEvent
  @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
      // Some client setup code
      LOGGER.info("HELLO FROM CLIENT SETUP");
      LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerScreens(final RegisterMenuScreensEvent event) {
      event.register(ModMenus.RECORDER_MENU.get(), RecorderScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.WORKER.get(), WorkerRenderer::new);
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(final EntityAttributeCreationEvent event) {
      event.put(ModEntities.WORKER.get(), WorkerEntity.createAttributes().build());
    }
  }

  public static Component translatable(final String prefix, final String key) {
    return Component.translatable(String.format("%s.%s.%s", prefix, MODID, key));
  }
}
