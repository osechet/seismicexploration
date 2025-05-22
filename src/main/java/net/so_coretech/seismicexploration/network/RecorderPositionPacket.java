package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.screen.RecorderScreen;
import org.slf4j.Logger;

public record RecorderPositionPacket(BlockPos blockPos) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<RecorderPositionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "recorder_position"));

    public static final StreamCodec<ByteBuf, RecorderPositionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            RecorderPositionPacket::blockPos,
            RecorderPositionPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /*
     * This method is called on the client side when the packet is received.
     * It updates the recorder position to display on the RecorderScreen as defaults.
     */
    public static void handle(final RecorderPositionPacket data, final IPayloadContext context) {
        LOGGER.debug("RecorderPositionPacket received");
        context.enqueueWork(() -> {
            // Here we are on the client side.
            RecorderScreen.setRecorderPosition(data.blockPos);
        });
    }
}
