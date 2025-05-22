package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public record DeploySensorsOrderPacket(int entityId, BlockPos startPos, Direction direction, int count,
                                       int gap) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<DeploySensorsOrderPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "deploy_sensors_order"));

    public static final StreamCodec<ByteBuf, DeploySensorsOrderPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            DeploySensorsOrderPacket::entityId,
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            DeploySensorsOrderPacket::startPos,
            ByteBufCodecs.fromCodec(Direction.CODEC),
            DeploySensorsOrderPacket::direction,
            ByteBufCodecs.VAR_INT,
            DeploySensorsOrderPacket::count,
            ByteBufCodecs.VAR_INT,
            DeploySensorsOrderPacket::gap,
            DeploySensorsOrderPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final DeploySensorsOrderPacket data, final IPayloadContext context) {
        LOGGER.debug("DeploySensorsOrderPacket received");
        context.enqueueWork(() -> {
            // Get the player who sent the packet
            final Player player = context.player();

            // Find the NPC entity by entityId
            final var entity = player.level().getEntity(data.entityId);
            if (entity instanceof final WorkerEntity workerEntity) {
                workerEntity.setFrozen(false);
                workerEntity.setDeploySensors(player, data.startPos, data.direction, data.count, data.gap);
            }
        });
    }

}
