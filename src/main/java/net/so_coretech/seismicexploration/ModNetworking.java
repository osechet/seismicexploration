package net.so_coretech.seismicexploration;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.so_coretech.seismicexploration.network.RecorderPositionPacket;
import net.so_coretech.seismicexploration.network.RecorderScreenValuesPacket;

public class ModNetworking {

    private static final int PROTOCOL_VERSION = 1;

    private static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "networking"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            // .acceptedVersions((status, version) -> status == VersionTest.Status.PRESENT)
            .simpleChannel();

    public static void register() {
        CHANNEL.play().flow(PacketFlow.SERVERBOUND).addMain(RecorderScreenValuesPacket.class, //
                StreamCodec.of((buf, msg) -> msg.toBytes(buf), //
                        (buf) -> new RecorderScreenValuesPacket(buf)),
                (msg, ctx) -> msg.handle(ctx));

        CHANNEL.play().flow(PacketFlow.CLIENTBOUND).addMain(RecorderPositionPacket.class, //
                StreamCodec.of((buf, msg) -> msg.toBytes(buf), //
                        (buf) -> new RecorderPositionPacket(buf)),
                (msg, ctx) -> msg.handle(ctx));
    }

    public static void sendToPlayer(final ServerPlayer player, final Object message) {
        CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(final Object message) {
        CHANNEL.send(message, PacketDistributor.SERVER.noArg());
    }
}
