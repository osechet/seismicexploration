package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;
import org.slf4j.Logger;

public record WorkerOrdersPacket(int entityId, int orderType) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<WorkerOrdersPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "worker_orders"));

    public static final StreamCodec<ByteBuf, WorkerOrdersPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            WorkerOrdersPacket::entityId,
            ByteBufCodecs.VAR_INT,
            WorkerOrdersPacket::orderType,
            WorkerOrdersPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final WorkerOrdersPacket data, final IPayloadContext context) {
        LOGGER.debug("WorkerOrdersPacket received");
        context.enqueueWork(() -> {
            // Get the player who sent the packet
            final Player player = context.player();

            // Find the NPC entity from the entityId
            final var entity = player.level().getEntity(data.entityId);
            if (entity instanceof final WorkerEntity workerEntity) {
                workerEntity.setFrozen(false);

                switch (OrderType.values()[data.orderType]) {
                    case FOLLOW_ME:
                        workerEntity.setFollowTarget(player);
                        break;
                    case DEPLOY_SENSORS:
                    case DEPLOY_CHARGES:
                    case OPERATE_BOOM_BOX:
                        LOGGER.error("Invalid order type: DEPLOY_SENSORS");
                        break;
                    default:
                        workerEntity.setFree(player);
                        break;
                }
            }
        });
    }

}
