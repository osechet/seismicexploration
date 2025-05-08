package net.so_code.seismicexploration.event;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.so_code.seismicexploration.block.SensorBlock;
import net.so_code.seismicexploration.spread.Spread;

@Mod.EventBusSubscriber
public class SensorBlockEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        BlockState state = event.getPlacedBlock();
        Block block = state.getBlock();
        if (block instanceof SensorBlock && event.getLevel() instanceof ServerLevel level) {
            Spread.getSpread(level).add(event.getPos());
            LOGGER.debug("Sensor added at {}", event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        if (block instanceof SensorBlock && event.getLevel() instanceof ServerLevel level) {
            Spread.getSpread(level).remove(event.getPos());
            LOGGER.debug("Sensor removed at {}", event.getPos());
        }
    }
}
