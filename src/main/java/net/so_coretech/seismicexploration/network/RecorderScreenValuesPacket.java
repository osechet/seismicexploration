package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.blockentity.RecorderBlockEntity;
import org.slf4j.Logger;

public record RecorderScreenValuesPacket(int xValue, int zValue, int axisValue,
                                         BlockPos blockPos) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<RecorderScreenValuesPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "recorder_screen_values"));

    public static final StreamCodec<ByteBuf, RecorderScreenValuesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            RecorderScreenValuesPacket::xValue,
            ByteBufCodecs.VAR_INT,
            RecorderScreenValuesPacket::zValue,
            ByteBufCodecs.VAR_INT,
            RecorderScreenValuesPacket::axisValue,
            ByteBufCodecs.fromCodec(BlockPos.CODEC),
            RecorderScreenValuesPacket::blockPos,
            RecorderScreenValuesPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /*
     * This method is called on the server side when the packet is received.
     * It updates the the new values input by the player.
     */
    public static void handle(final RecorderScreenValuesPacket data, final IPayloadContext context) {
        LOGGER.debug("RecorderScreenValuesPacket received");
        context.enqueueWork(() -> {
            // Here we are on the server side.
            final Player player = context.player();
            final BlockEntity blockEntity = player.level().getBlockEntity(data.blockPos);
            if (blockEntity instanceof final RecorderBlockEntity recorderBlockEntity) {
                LOGGER.debug("blockEntity found - updating");
                recorderBlockEntity.setSliderValues(data.xValue, data.zValue, data.axisValue);
            } else {
                LOGGER.warn("blockEntity not found");
            }
        });
    }
}
