package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.so_coretech.seismicexploration.entity.WorkerEntity;
import org.slf4j.Logger;

public class DeploySensorsOrderPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int entityId;
    private final BlockPos startPos;
    private final Direction direction;
    private final int count;
    private final int gap;

    public DeploySensorsOrderPacket(final int entityId, final BlockPos startPos, final Direction direction,
                                    final int count,
                                    final int gap) {
        this.entityId = entityId;
        this.startPos = startPos;
        this.direction = direction;
        this.count = count;
        this.gap = gap;
    }

    public DeploySensorsOrderPacket(final FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.startPos = buf.readBlockPos();
        this.direction = buf.readEnum(Direction.class);
        this.count = buf.readInt();
        this.gap = buf.readInt();
    }

    public void toBytes(final FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBlockPos(this.startPos);
        buf.writeEnum(this.direction);
        buf.writeInt(this.count);
        buf.writeInt(this.gap);
    }

    public void handle(final CustomPayloadEvent.Context context) {
        LOGGER.debug("DeploySensorsOrderPacket received");
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
                workerEntity.setDeploySensors(player, this.startPos, this.direction, this.count, this.gap);
            }
        });
    }

}
