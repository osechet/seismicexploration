package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.screen.WorkerOrderScreen;
import org.slf4j.Logger;

public record OpenWorkerOrderMenuPacket(int entityId, BlockPos playerPos)
    implements CustomPacketPayload {

  private static final Logger LOGGER = LogUtils.getLogger();

  public static final CustomPacketPayload.Type<OpenWorkerOrderMenuPacket> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(
              SeismicExploration.MODID, "open_worker_order_menu"));

  public static final StreamCodec<ByteBuf, OpenWorkerOrderMenuPacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          OpenWorkerOrderMenuPacket::entityId,
          ByteBufCodecs.fromCodec(BlockPos.CODEC),
          OpenWorkerOrderMenuPacket::playerPos,
          OpenWorkerOrderMenuPacket::new);

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /*
   * This method is called on the client side when the packet is received.
   * It opens the WorkerOrderScreen for the worker with the given entityId and provide the player position for the UI.
   */
  public static void handle(final OpenWorkerOrderMenuPacket data, final IPayloadContext context) {
    LOGGER.debug("OpenWorkerOrderMenuPacket received");
    context.enqueueWork(
        () -> {
          final Minecraft mc = Minecraft.getInstance();
          mc.setScreen(new WorkerOrderScreen(data.entityId, data.playerPos));
        });
  }
}
