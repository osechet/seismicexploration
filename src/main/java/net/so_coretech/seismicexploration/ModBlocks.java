package net.so_coretech.seismicexploration;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.so_coretech.seismicexploration.block.BoomBoxBlock;
import net.so_coretech.seismicexploration.block.ChargeBlock;
import net.so_coretech.seismicexploration.block.PetroleumBlock;
import net.so_coretech.seismicexploration.block.RecorderBlock;
import net.so_coretech.seismicexploration.block.SensorBlock;

public class ModBlocks {
  private static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(SeismicExploration.MODID);

  //
  // Register blocks
  //

  public static final DeferredBlock<Block> BOOM_BOX =
      BLOCKS.registerBlock(
          "boom_box",
          BoomBoxBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.COLOR_BLUE) // the color on the map
              .sound(SoundType.CROP) // the sound made when placed or destroyed
              .noOcclusion() // avoid display issues with bigger surrounding blocks
          );

  public static final DeferredBlock<Block> DFU =
      BLOCKS.registerBlock(
          "dfu",
          SensorBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.COLOR_BLUE) // the color on the map
              .sound(SoundType.CROP) // the sound made when placed or destroyed
              .noOcclusion() // avoid display issues with bigger surrounding blocks
          );

  public static final DeferredBlock<Block> DFU3C =
      BLOCKS.registerBlock(
          "dfu3c",
          SensorBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.COLOR_BLUE) // the color on the map
              .sound(SoundType.CROP) // the sound made when placed or destroyed
              .noOcclusion() // avoid display issues with bigger surrounding blocks
          );

  public static final DeferredBlock<Block> AFU =
      BLOCKS.registerBlock(
          "afu",
          SensorBlock::new,
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

  public static final DeferredBlock<Block> PETROLEUM =
      BLOCKS.registerBlock(
          "petroleum",
          PetroleumBlock::new,
          BlockBehaviour.Properties.of() // Properties:
              .mapColor(MapColor.COLOR_BLACK) // the color on the map
              .sound(SoundType.CROP) // the sound made when placed or destroyed
              .noOcclusion());

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
