package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;
import org.slf4j.Logger;

public class WorkerOrdersPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int entityId;
    private final OrderType orderType;

    public WorkerOrdersPacket(final int entityId, final OrderType orderType) {
        this.entityId = entityId;
        this.orderType = orderType;
    }

    public WorkerOrdersPacket(final FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.orderType = OrderType.values()[buf.readInt()];
    }

    public void toBytes(final FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.orderType.ordinal());
    }

    public void handle(final CustomPayloadEvent.Context context) {
        LOGGER.debug("WorkerOrdersPacket received");
        context.enqueueWork(() -> {
            // Get the player who sent the packet
            final var player = context.getSender();
            if (player == null) {
                return;
            }

            // Find the NPC entity by entityId
            final var entity = player.level().getEntity(entityId);
            if (entity instanceof final WorkerEntity workerEntity) {
                workerEntity.setFrozen(false);

                switch (orderType) {
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
