package net.so_coretech.seismicexploration;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.so_coretech.seismicexploration.network.*;
import org.slf4j.Logger;

@EventBusSubscriber(modid = SeismicExploration.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

  private static final Logger LOGGER = LogUtils.getLogger();

  private static final String PROTOCOL_VERSION = "1";

  @SubscribeEvent
  public static void register(final RegisterPayloadHandlersEvent event) {
    LOGGER.debug("Registering payload handlers");

    // Sets the current network version
    final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

    // Packet to send the values from the RecorderScreen to the RecorderBlockEntity
    registrar.playToServer(
        RecorderScreenValuesPacket.TYPE,
        RecorderScreenValuesPacket.STREAM_CODEC,
        RecorderScreenValuesPacket::handle);

    // Packet to send the recorder position to the client, used by the RecorderScreen
    registrar.playToClient(
        RecorderPositionPacket.TYPE,
        RecorderPositionPacket.STREAM_CODEC,
        RecorderPositionPacket::handle);

    // Packet to open the worker order screen
    if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
      registrar.playToClient(
          OpenWorkerOrderMenuPacket.TYPE,
          OpenWorkerOrderMenuPacket.STREAM_CODEC,
          OpenWorkerOrderMenuPacket::handle);
    }

    // Packet to notify the server that the worker order screen has been closed
    registrar.playToServer(
        OnCloseWorkerOrderMenuPacket.TYPE,
        OnCloseWorkerOrderMenuPacket.STREAM_CODEC,
        OnCloseWorkerOrderMenuPacket::handle);

    // Packet to give a generic order to a worker entity
    registrar.playToServer(
        WorkerOrdersPacket.TYPE, WorkerOrdersPacket.STREAM_CODEC, WorkerOrdersPacket::handle);

    // Packet to give a "deploy sensors" order to a worker entity
    registrar.playToServer(
        DeploySensorsOrderPacket.TYPE,
        DeploySensorsOrderPacket.STREAM_CODEC,
        DeploySensorsOrderPacket::handle);
  }
}
