package net.so_coretech.seismicexploration.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.so_coretech.seismicexploration.SeismicExploration;
import org.slf4j.Logger;

/**
 * This packet is sent from the server to the client to display smoke particles on a primed charge.
 */
public record PrimedSmokePacket(BlockPos blockPos) implements CustomPacketPayload {

  private static final Logger LOGGER = LogUtils.getLogger();

  public static final CustomPacketPayload.Type<PrimedSmokePacket> TYPE =
      new CustomPacketPayload.Type<>(
          ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "primed_smoke"));

  public static final StreamCodec<ByteBuf, PrimedSmokePacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.fromCodec(BlockPos.CODEC),
          PrimedSmokePacket::blockPos,
          PrimedSmokePacket::new);

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /*
   * This method is called on the client side when the packet is received.
   * It adds smoke particles at the given position.
   */
  public static void handle(final PrimedSmokePacket data, final IPayloadContext context) {
    context.enqueueWork(
        () -> {
          // This code runs on the client thread
          Level level = context.player().level();

          // Spawn smoke particles
          for (int i = 0; i < 5; i++) {
            double x = data.blockPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
            double y = data.blockPos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
            double z = data.blockPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.05D, 0.0D); // Upward motion
          }
        });
  }
}
