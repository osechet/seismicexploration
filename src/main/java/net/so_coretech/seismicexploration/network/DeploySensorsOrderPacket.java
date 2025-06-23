package net.so_coretech.seismicexploration.network;

import com.google.gson.JsonObject;
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
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;
import net.so_coretech.seismicexploration.entity.ai.task.ITask;
import net.so_coretech.seismicexploration.entity.ai.task.TaskFactory;
import org.slf4j.Logger;

public record DeploySensorsOrderPacket(
    int entityId, BlockPos startPos, Direction direction, int count, int gap)
    implements CustomPacketPayload {

  private static final Logger LOGGER = LogUtils.getLogger();

  public static final CustomPacketPayload.Type<DeploySensorsOrderPacket> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "deploy_sensors_order"));

  public static final StreamCodec<ByteBuf, DeploySensorsOrderPacket> STREAM_CODEC =
      StreamCodec.composite(
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
          DeploySensorsOrderPacket::new);

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(final DeploySensorsOrderPacket data, final IPayloadContext context) {
    LOGGER.debug("DeploySensorsOrderPacket received");
    context.enqueueWork(
        () -> {
          // Get the player who sent the packet
          final Player player = context.player();

          // Find the NPC entity by entityId
          final var entity = player.level().getEntity(data.entityId);
          if (entity instanceof final WorkerEntity workerEntity) {
            workerEntity.setFrozen(false);

            final JsonObject params = new JsonObject();
            params.addProperty("startX", data.startPos().getX());
            // Assuming startPos from packet is ground level or DeployTask's getGroundLevel handles
            // Y.
            // For TaskFactory, we need to provide a Y. Let's use the Y from the packet for now.
            params.addProperty("startY", data.startPos().getY());
            params.addProperty("startZ", data.startPos().getZ());
            params.addProperty("direction", data.direction().getName());
            params.addProperty("count", data.count());
            params.addProperty("gap", data.gap());

            final ITask deployTask =
                TaskFactory.createTask(OrderType.DEPLOY_SENSORS, workerEntity, player, params);

            if (deployTask != null) {
              workerEntity.assignTask(deployTask, player);
            } else {
              LOGGER.error(
                  "Failed to create DeployTask for DEPLOY_SENSORS order. Worker: {}",
                  workerEntity.getId());
              // Optionally assign a default task or send a failure message to player
            }
          }
        });
  }
}
