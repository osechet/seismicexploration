package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.so_coretech.seismicexploration.blockentity.RecorderBlockEntity;
import org.slf4j.Logger;

public class RecorderScreenValuesPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int xValue;
    private final int zValue;
    private final int axisValue;
    private final BlockPos blockPos;

    public RecorderScreenValuesPacket(final int xValue, final int zValue, final int axisValue,
                                      final BlockPos blockPos) {
        this.xValue = xValue;
        this.zValue = zValue;
        this.axisValue = axisValue;
        this.blockPos = blockPos;
    }

    public RecorderScreenValuesPacket(final FriendlyByteBuf buf) {
        this.xValue = buf.readInt();
        this.zValue = buf.readInt();
        this.axisValue = buf.readInt();
        this.blockPos = buf.readBlockPos();
    }

    public void toBytes(final FriendlyByteBuf buf) {
        buf.writeInt(this.xValue);
        buf.writeInt(this.zValue);
        buf.writeInt(this.axisValue);
        buf.writeBlockPos(this.blockPos);
    }

    public void handle(final CustomPayloadEvent.Context context) {
        LOGGER.debug("RecorderScreenValuesPacket received");
        context.enqueueWork(() -> {
            // Here we are on the server side.
            final ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            final BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
            if (blockEntity instanceof final RecorderBlockEntity recorderBlockEntity) {
                LOGGER.debug("blockEntity found - updating");
                recorderBlockEntity.setSliderValues(xValue, zValue, axisValue);
            } else {
                LOGGER.debug("blockEntity not found");
            }
        });
    }
}
