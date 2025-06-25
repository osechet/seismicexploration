package net.so_coretech.seismicexploration;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.so_coretech.seismicexploration.block.ChargeBlock;
import net.so_coretech.seismicexploration.block.DFUBlock;
import net.so_coretech.seismicexploration.block.RecorderBlock;

public class ModBlocks {

  private static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(SeismicExploration.MODID);

  //
  // Register blocks
  //

  public static final DeferredBlock<Block> DFU =
      BLOCKS.registerBlock(
          "dfu",
          DFUBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.COLOR_BLUE) // the color on the map
              .sound(SoundType.CROP) // the sound made when placed or destroyed
              .noOcclusion() // avoid display issues with bigger surrounding blocks
          );

  public static final DeferredBlock<Block> RECORDER =
      BLOCKS.registerBlock(
          "recorder",
          RecorderBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.METAL) // the color on the map
              .sound(SoundType.STONE) // the sound made when placed or destroyed
              .noOcclusion() // avoid display issues with bigger surrounding blocks
          );

  public static final DeferredBlock<Block> CHARGE =
      BLOCKS.registerBlock(
          "charge",
          ChargeBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.COLOR_ORANGE) // the color on the map
              .sound(SoundType.STONE) // the sound made when placed or destroyed
              .noOcclusion() // avoid display issues with bigger surrounding blocks
          );

  //
  // Utilities
  //

  protected static void register(final IEventBus eventBus) {
    BLOCKS.register(eventBus);
  }
}
