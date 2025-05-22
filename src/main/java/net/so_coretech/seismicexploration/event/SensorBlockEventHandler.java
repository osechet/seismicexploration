package net.so_coretech.seismicexploration.event;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.so_coretech.seismicexploration.block.SensorBlock;
import net.so_coretech.seismicexploration.spread.Spread;
import org.slf4j.Logger;

@EventBusSubscriber
public class SensorBlockEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
        final BlockState state = event.getPlacedBlock();
        final Block block = state.getBlock();
        if (block instanceof SensorBlock && event.getLevel() instanceof final ServerLevel level) {
            Spread.getSpread(level).add(event.getPos());
            LOGGER.debug("Sensor added at {}", event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(final BlockEvent.BreakEvent event) {
        final BlockState state = event.getState();
        final Block block = state.getBlock();
        if (block instanceof SensorBlock && event.getLevel() instanceof final ServerLevel level) {
            Spread.getSpread(level).remove(event.getPos());
            LOGGER.debug("Sensor removed at {}", event.getPos());
        }
    }
}
