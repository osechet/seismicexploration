package net.so_coretech.seismicexploration;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.so_coretech.seismicexploration.network.*;

public class ModNetworking {

    private static final int PROTOCOL_VERSION = 1;

    private static final SimpleChannel CHANNEL = ChannelBuilder
        .named(ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "networking"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        // .acceptedVersions((status, version) -> status == VersionTest.Status.PRESENT)
        .simpleChannel();

    public static void register() {
        // Packet to send the RecorderScreen values to the RecorderBlockEntity
        CHANNEL.play().flow(PacketFlow.SERVERBOUND).addMain(RecorderScreenValuesPacket.class,
            StreamCodec.of((buf, msg) -> msg.toBytes(buf),
                RecorderScreenValuesPacket::new),
            RecorderScreenValuesPacket::handle);

        // Packet to send the recorder position to the client, used by the RecorderScreen
        CHANNEL.play().flow(PacketFlow.CLIENTBOUND).addMain(RecorderPositionPacket.class,
            StreamCodec.of((buf, msg) -> msg.toBytes(buf),
                RecorderPositionPacket::new),
            RecorderPositionPacket::handle);

        // Packet to open the worker order screen
        if (FMLEnvironment.dist.isClient()) {
            CHANNEL.play().flow(PacketFlow.CLIENTBOUND).addMain(OpenWorkerOrderMenuPacket.class,
                StreamCodec.of((buf, msg) -> msg.toBytes(buf),
                    OpenWorkerOrderMenuPacket::new),
                OpenWorkerOrderMenuPacket::handle);
        }

        // Packet to notify the server that the worker order screen has been closed
        CHANNEL.play().flow(PacketFlow.SERVERBOUND).addMain(OnCloseWorkerOrderMenuPacket.class,
            StreamCodec.of((buf, msg) -> msg.toBytes(buf),
                OnCloseWorkerOrderMenuPacket::new),
            OnCloseWorkerOrderMenuPacket::handle);

        // Packet to give a generic order to a worker entity
        CHANNEL.play().flow(PacketFlow.SERVERBOUND).addMain(WorkerOrdersPacket.class,
            StreamCodec.of((buf, msg) -> msg.toBytes(buf),
                WorkerOrdersPacket::new),
            WorkerOrdersPacket::handle);

        // Packet to give a deploy sensors order to a worker entity
        CHANNEL.play().flow(PacketFlow.SERVERBOUND).addMain(DeploySensorsOrderPacket.class,
            StreamCodec.of((buf, msg) -> msg.toBytes(buf),
                DeploySensorsOrderPacket::new),
            DeploySensorsOrderPacket::handle);
    }

    public static void sendToPlayer(final ServerPlayer player, final Object message) {
        CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(final Object message) {
        CHANNEL.send(message, PacketDistributor.SERVER.noArg());
    }
}
