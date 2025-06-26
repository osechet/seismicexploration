package net.so_coretech.seismicexploration;

import java.util.function.BiFunction;
import net.minecraft.world.item.BlockItem;
// import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
// import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

  private static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(SeismicExploration.MODID);

  //
  // Register items
  //

  public static final DeferredItem<Item> DFU =
      registerBlock(ModBlocks.DFU, BlockItem::new, new Item.Properties());

  public static final DeferredItem<Item> DFU3C =
      registerBlock(ModBlocks.DFU3C, BlockItem::new, new Item.Properties());

  public static final DeferredItem<Item> AFU =
      registerBlock(ModBlocks.AFU, BlockItem::new, new Item.Properties());

  public static final DeferredItem<Item> RECORDER =
      registerBlock(ModBlocks.RECORDER, BlockItem::new, new Item.Properties());

  public static final DeferredItem<Item> CHARGE =
      registerBlock(ModBlocks.CHARGE, BlockItem::new, new Item.Properties());

  public static final DeferredItem<Item> FIELD_MONITOR =
      ITEMS.registerSimpleItem("field_monitor", new Item.Properties());

  public static final DeferredItem<Item> BLASTER =
      ITEMS.registerSimpleItem("blaster", new Item.Properties());

  public static final DeferredItem<Item> MEMS =
      ITEMS.registerSimpleItem("mems", new Item.Properties().rarity(Rarity.COMMON).stacksTo(64));

  public static final DeferredItem<Item> PLA =
      ITEMS.registerSimpleItem("pla", new Item.Properties().rarity(Rarity.COMMON).stacksTo(64));

  public static final DeferredItem<Item> GEOPHONE =
      ITEMS.registerSimpleItem(
          "geophone", new Item.Properties().rarity(Rarity.COMMON).stacksTo(64));

  public static final DeferredItem<Item> FERMENTED_SUGAR =
      ITEMS.registerSimpleItem(
          "fermented_sugar", new Item.Properties().rarity(Rarity.COMMON).stacksTo(64));

  //
  // Utilities
  //

  private static <T extends Block> DeferredItem<Item> registerBlock(
      final DeferredBlock<T> ro,
      final BiFunction<T, Item.Properties, Item> factory,
      final Item.Properties properties) {
    return ITEMS.registerItem(
        ro.getId().getPath(),
        (final Item.Properties props) -> factory.apply(ro.get(), props),
        properties.useBlockDescriptionPrefix());
  }

  protected static void register(final IEventBus eventBus) {
    ITEMS.register(eventBus);
  }
}
