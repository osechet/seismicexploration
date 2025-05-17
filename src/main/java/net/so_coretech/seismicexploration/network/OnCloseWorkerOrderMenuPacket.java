package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public class OnCloseWorkerOrderMenuPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int entityId;

    public OnCloseWorkerOrderMenuPacket(final int entityId) {
        this.entityId = entityId;
    }

    public OnCloseWorkerOrderMenuPacket(final FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void toBytes(final FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(final CustomPayloadEvent.Context context) {
        LOGGER.debug("OnCloseWorkerOrderMenuPacket received");
        context.enqueueWork(() -> {
            // Get the server player from the context
            final var player = context.getSender();
            if (player == null) return; // Defensive: should always be server-side

            // Get the entity by id
            final var entity = player.level().getEntity(entityId);
            if (entity instanceof final WorkerEntity workerEntity) {
                workerEntity.setFrozen(false);
            }
        });
    }
}
