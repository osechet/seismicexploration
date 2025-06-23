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
import net.so_coretech.seismicexploration.entity.ai.task.ITask;
import net.so_coretech.seismicexploration.entity.ai.task.TaskFactory;
import org.slf4j.Logger;

public record WorkerOrdersPacket(int entityId, int orderType) implements CustomPacketPayload {

  private static final Logger LOGGER = LogUtils.getLogger();

  public static final CustomPacketPayload.Type<WorkerOrdersPacket> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "worker_orders"));

  public static final StreamCodec<ByteBuf, WorkerOrdersPacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          WorkerOrdersPacket::entityId,
          ByteBufCodecs.VAR_INT,
          WorkerOrdersPacket::orderType,
          WorkerOrdersPacket::new);

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(final WorkerOrdersPacket data, final IPayloadContext context) {
    LOGGER.debug("WorkerOrdersPacket received");
    context.enqueueWork(
        () -> {
          // Get the player who sent the packet
          final Player player = context.player();

          // Find the NPC entity from the entityId
          final var entity = player.level().getEntity(data.entityId);
          if (entity instanceof final WorkerEntity workerEntity) {
            workerEntity.setFrozen(false);
            final OrderType orderType = OrderType.values()[data.orderType];
            ITask newTask = null;

            // This packet is intended for simple orders without extra parameters.
            // Complex orders like DEPLOY_SENSORS have their own packets.
            if (orderType == OrderType.FOLLOW_ME || orderType == OrderType.FREE_ROAMING) {
              // For other future simple orders, add them to this condition.
              newTask = TaskFactory.createTask(orderType, workerEntity, player, null);
            } else if (orderType == OrderType.DEPLOY_SENSORS
                || orderType == OrderType.DEPLOY_CHARGES
                || orderType == OrderType.OPERATE_BOOM_BOX) {
              LOGGER.error(
                  "Received WorkerOrdersPacket for complex order type {} which should have its own dedicated packet. This packet will be ignored.",
                  orderType);
              // newTask remains null, so no task will be assigned.
            } else {
              LOGGER.warn(
                  "Received WorkerOrdersPacket with unhandled or unexpected OrderType: {}. Worker will clear its current task.",
                  orderType);
              // newTask remains null, leading to current task being cleared.
            }

            // Assign the task (which might be null, e.g., for FREE_ROAMING or unhandled types).
            // workerEntity.assignTask(null, player) correctly clears the current task.
            workerEntity.assignTask(newTask, player);

            // Optional: Log if a task was expected for a simple order but factory returned null
            // (e.g., FOLLOW_ME failed because player was null, though factory handles this).
            if (newTask == null
                && (orderType == OrderType.FOLLOW_ME /* || other simple orders */)) {
              LOGGER.warn(
                  "TaskFactory returned null for simple orderType: {}. Worker {} will clear its current task.",
                  orderType,
                  workerEntity.getId());
            }
          }
        });
  }
}
