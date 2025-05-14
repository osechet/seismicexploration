package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.so_coretech.seismicexploration.screen.RecorderScreen;
import org.slf4j.Logger;

public class RecorderPositionPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final BlockPos blockPos;

    public RecorderPositionPacket(final BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public RecorderPositionPacket(final FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void toBytes(final FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    public boolean handle(final CustomPayloadEvent.Context context) {
        LOGGER.debug("RecorderPositionPacket received");
        context.enqueueWork(() -> {
            // Here we are on the client side.
            RecorderScreen.setRecorderPosition(blockPos);
        });

        return true;
    }
}
