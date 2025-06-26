package net.so_coretech.seismicexploration.blockentity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.block.ChargeBlock;
import net.so_coretech.seismicexploration.network.PrimedSmokePacket;
import net.so_coretech.seismicexploration.spread.Spread;
import org.slf4j.Logger;

public class ChargeBlockEntity extends BlockEntity implements TickableBlockEntity {

  private static final Logger LOGGER = LogUtils.getLogger();

  private static final int FUSE_DURATION = 100; // 5 seconds (20 ticks per second)
  private static final float EXPLOSION_RADIUS = 2.5f;

  private int fuseTicks = -1; // -1 means not primed
  private boolean hasExploded = false;

  public ChargeBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.CHARGE_ENTITY.get(), pos, state);
  }

  @Override
  protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
    LOGGER.trace(
        "loadAdditional - {}",
        level == null ? "server" : level.isClientSide() ? "client" : "server");
    super.loadAdditional(tag, registry);
    fuseTicks = tag.getIntOr("fuseTicks", -1);
    hasExploded = tag.getBooleanOr("hasExploded", false);
    // Ensure block state is consistent with loaded data
    if (level != null && !level.isClientSide()) {
      level.setBlock(
          getBlockPos(), getBlockState().setValue(ChargeBlock.PRIMED, fuseTicks != -1), 3);
    }
  }

  @Override
  protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
    LOGGER.trace(
        "saveAdditional - {}", Objects.requireNonNull(level).isClientSide() ? "client" : "server");
    super.saveAdditional(tag, registry);
    tag.putInt("fuseTicks", fuseTicks);
    tag.putBoolean("hasExploded", hasExploded);
  }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(
      @Nullable final Connection connection,
      @Nullable final ClientboundBlockEntityDataPacket pkt,
      @Nullable final Provider lookup) {
    CompoundTag tag = pkt.getTag();
    if (tag != null) {
      fuseTicks = tag.getIntOr("fuseTicks", -1);
      // Update client-side block state based on server state
      if (level != null && level.isClientSide()) {
        BlockState state = getBlockState();
        if (state.hasProperty(ChargeBlock.PRIMED)) {
          level.setBlock(
              getBlockPos(),
              state.setValue(ChargeBlock.PRIMED, tag.getBooleanOr("PrimedState", false)),
              3);
        }
      }
    }
  }

  @Override
  public CompoundTag getUpdateTag(final Provider registries) {
    LOGGER.trace(
        "getUpdateTag - {}", Objects.requireNonNull(level).isClientSide() ? "client" : "server");
    final CompoundTag tag = super.getUpdateTag(registries);
    this.saveAdditional(tag, registries);
    tag.putBoolean(
        "PrimedState", getBlockState().getValue(ChargeBlock.PRIMED)); // Sync primed state
    return tag;
  }

  private void sendUpdateToClients() {
    if (level instanceof ServerLevel serverLevel) {
      serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }
  }

  /** Primes the charge. */
  public void prime() {
    if (level == null || level.isClientSide) {
      return;
    }
    if (fuseTicks < 0) {
      LOGGER.debug("Priming charge at {}", worldPosition);
      fuseTicks = FUSE_DURATION;
      // Set block to lit
      level.setBlock(worldPosition, getBlockState().setValue(ChargeBlock.LIT, true), 3);
      setChanged();
      sendUpdateToClients();
    }
  }

  /** Handles the explosion logic when the fuse runs out. */
  private void explode() {
    LOGGER.debug("Charge detonated at {}", worldPosition);
    // Remove the block
    level.removeBlock(worldPosition, false);
    // Fire shot
    fire();
    // Final explosion
    final double x = worldPosition.getX() + 0.5;
    final double y = worldPosition.getY() + 0.25;
    final double z = worldPosition.getZ() + 0.5;
    // Play explosion sound and particles
    level.explode(null, x, y, z, EXPLOSION_RADIUS, ExplosionInteraction.NONE);
    hasExploded = true;
    setChanged();
  }

  private void fire() {
    if (level instanceof final ServerLevel serverLevel) {
      LOGGER.debug("Charge firing shot at {}", worldPosition);
      final Set<BlockPos> positions = Spread.getSpread(serverLevel).getPlacedSensors();
      for (final BlockPos sensorPos : positions) {
        final BlockEntity be = serverLevel.getBlockEntity(sensorPos);
        if (be instanceof final SensorBlockEntity blockEntity) {
          blockEntity.record(worldPosition);
        } else {
          // There is no sensor block entity at this position, so we remove it from the spread
          Spread.getSpread(serverLevel).remove(sensorPos);
        }
      }
    }
  }

  @Override
  public void tick() {
    if (fuseTicks > 0) {
      if (fuseTicks % 5 == 0) { // Send particle updates every 5 ticks (0.25 seconds)
        PacketDistributor.sendToAllPlayers(new PrimedSmokePacket(worldPosition));
      }

      // Blink every 5 ticks
      boolean lit = (fuseTicks / 5) % 2 == 0;
      BlockState state = getBlockState();
      if (state.getValue(net.so_coretech.seismicexploration.block.ChargeBlock.LIT) != lit) {
        level.setBlock(
            worldPosition,
            state.setValue(net.so_coretech.seismicexploration.block.ChargeBlock.LIT, lit),
            3);
      }
      fuseTicks--;
      if (fuseTicks == 0) {
        explode();
      }
    }
  }
}
