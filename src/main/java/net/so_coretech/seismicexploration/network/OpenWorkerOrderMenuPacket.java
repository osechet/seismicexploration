package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.so_coretech.seismicexploration.screen.WorkerOrderScreen;
import org.slf4j.Logger;

public class OpenWorkerOrderMenuPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int entityId;
    private final BlockPos playerPos;

    public OpenWorkerOrderMenuPacket(final int entityId, final BlockPos playerPos) {
        this.entityId = entityId;
        this.playerPos = playerPos;
    }

    public OpenWorkerOrderMenuPacket(final FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.playerPos = buf.readBlockPos();
    }

    public void toBytes(final FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBlockPos(playerPos);
    }

    @OnlyIn(Dist.CLIENT)
    public void handle(final CustomPayloadEvent.Context context) {
        LOGGER.debug("OpenWorkerOrderMenuPacket received");
        context.enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new WorkerOrderScreen(entityId, playerPos));
        });
    }
}
