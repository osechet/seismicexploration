package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public record OnCloseWorkerOrderMenuPacket(int entityId) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<OnCloseWorkerOrderMenuPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "on_close_worker_order_menu"));

    public static final StreamCodec<ByteBuf, OnCloseWorkerOrderMenuPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OnCloseWorkerOrderMenuPacket::entityId,
            OnCloseWorkerOrderMenuPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /*
     * This method is called on the server side when the packet is received.
     * It closes the WorkerOrderScreen for the worker with the given entityId.
     */
    public static void handle(final OnCloseWorkerOrderMenuPacket data, final IPayloadContext context) {
        LOGGER.debug("OnCloseWorkerOrderMenuPacket received");
        context.enqueueWork(() -> {
            // Get the server player from the context
            final Player player = context.player();

            // Get the entity by id
            final Entity entity = player.level().getEntity(data.entityId);
            if (entity instanceof final WorkerEntity workerEntity) {
                workerEntity.setFrozen(false);
            }
        });
    }
}
