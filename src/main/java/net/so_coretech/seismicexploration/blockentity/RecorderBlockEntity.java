package net.so_coretech.seismicexploration.blockentity;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.so_coretech.seismicexploration.ModBlockEntities;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.client.ClientLevelDataManager;
import net.so_coretech.seismicexploration.menu.RecorderMenu;
import net.so_coretech.seismicexploration.screen.RecorderScreen;
import net.so_coretech.seismicexploration.spread.SliceData;
import net.so_coretech.seismicexploration.spread.Spread;
import org.slf4j.Logger;

public class RecorderBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {

  private static final Logger LOGGER = LogUtils.getLogger();

  private Map<BlockPos, Byte> blocks = new HashMap<>();
  private boolean shouldRetrieve = false;

  private int xValue;
  private int zValue;
  private int axisValue;
  private SliceData sliceData = new SliceData();

  public RecorderBlockEntity(final BlockPos pos, final BlockState state) {
    super(ModBlockEntities.RECORDER_ENTITY.get(), pos, state);
    setSliderValues(pos.getX(), pos.getZ(), RecorderScreen.AXIS_X);
  }

  public void setSliderValues(final int xValue, final int zValue, final int axisValue) {
    LOGGER.debug("setSliderValues({}, {}, {})", xValue, zValue, axisValue);
    this.xValue = xValue;
    this.zValue = zValue;
    this.axisValue = axisValue;
    this.sliceData = SliceData.fromBlocks(xValue, zValue, axisValue, blocks);
    setChanged();
    if (level != null) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    } else {
      LOGGER.warn("level not yet initialized");
    }
  }

  public void powerOn() {
    LOGGER.debug("Recorder at {} retrieving data", worldPosition);
    shouldRetrieve = true;
  }

  @Override
  public Component getDisplayName() {
    // No need to use a translatable since the text is never displayed
    return Component.literal("Recorder");
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(
      final int containerId, final Inventory playerInventory, final Player player) {
    final ContainerLevelAccess access;
    if (level != null) {
      access = ContainerLevelAccess.create(level, worldPosition);
    } else {
      access = ContainerLevelAccess.NULL;
    }
    return new RecorderMenu(containerId, playerInventory, access);
  }

  @Override
  protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
    LOGGER.debug(
        "loadAdditional - {}",
        level == null ? "server" : level.isClientSide() ? "client" : "server");
    super.loadAdditional(tag, registry);

    final CompoundTag compound = tag.getCompoundOrEmpty(SeismicExploration.MODID);

    xValue = compound.getIntOr("xValue", worldPosition.getX());
    zValue = compound.getIntOr("zValue", worldPosition.getZ());
    axisValue = compound.getIntOr("axisValue", RecorderScreen.AXIS_X);
    sliceData = SliceData.fromNBT(compound.getCompoundOrEmpty("sliceData"));

    LOGGER.debug(
        "loaded - xValue = {}, zValue = {}, axisValue = {}, sliceData = {}",
        xValue,
        zValue,
        axisValue,
        sliceData);
  }

  @Override
  protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
    LOGGER.debug(
        "saveAdditional - {}", Objects.requireNonNull(level).isClientSide() ? "client" : "server");
    super.saveAdditional(tag, registry);

    LOGGER.debug(
        "saving - xValue = {}, zValue = {}, axisValue = {}, sliceData = {}",
        xValue,
        zValue,
        axisValue,
        sliceData);
    final CompoundTag compound = new CompoundTag();

    compound.putInt("xValue", xValue);
    compound.putInt("zValue", zValue);
    compound.putInt("axisValue", axisValue);
    compound.put("sliceData", sliceData.serializeNBT());

    tag.put(SeismicExploration.MODID, compound);
  }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  /**
   * Called when the block is updated. This is called on the client side when the server calls
   * sendBlockUpdated.
   *
   * @param connection The connection to use.
   * @param pkt The received packet.
   * @param lookup The lookup to use.
   */
  @Override
  public void onDataPacket(
      @Nullable final Connection connection,
      @Nullable final ClientboundBlockEntityDataPacket pkt,
      @Nullable final Provider lookup) {
    super.onDataPacket(connection, pkt, lookup);
    LOGGER.debug("onDataPacket - received {}", this.sliceData);
    ClientLevelDataManager.get()
        .setRecorderParameters(this.sliceData.centerX, this.sliceData.centerZ, this.sliceData.axis);
    ClientLevelDataManager.get().setSlice(this.sliceData);
  }

  /**
   * Called when the block is updated. This is used to send the data to the client.
   *
   * @param registries The registries to use.
   * @return The update tag.
   */
  @Override
  public CompoundTag getUpdateTag(final Provider registries) {
    LOGGER.debug(
        "getUpdateTag - {}", Objects.requireNonNull(level).isClientSide() ? "client" : "server");
    final CompoundTag tag = super.getUpdateTag(registries);
    this.saveAdditional(tag, registries);
    return tag;
  }

  @Override
  public void handleUpdateTag(final CompoundTag tag, final Provider holders) {
    super.handleUpdateTag(tag, holders);

    if (level != null && level.isClientSide()) {
      ClientLevelDataManager.get()
          .setRecorderParameters(
              this.sliceData.centerX, this.sliceData.centerZ, this.sliceData.axis);
      ClientLevelDataManager.get().setSlice(this.sliceData);
    }
  }

  @Override
  public void tick() {
    if (shouldRetrieve) {
      if (level != null) {
        final Level lvl = level;

        if (lvl instanceof final ServerLevel serverLevel) {
          final Set<BlockPos> placedSensors = Spread.getSpread(serverLevel).getPlacedSensors();
          this.blocks =
              placedSensors.stream() //
                  .map(pos -> (SensorBlockEntity) serverLevel.getBlockEntity(pos)) //
                  .map(sensor -> Objects.requireNonNull(sensor).getBlocks()) //
                  .flatMap(map -> map.entrySet().stream()) //
                  .collect(
                      Collectors.toMap(
                          Map.Entry::getKey, //
                          entry -> entry.getValue().getPackedId(Brightness.NORMAL), //
                          (existing, replacement) -> existing // keep the first value
                          // found
                          ));
          LOGGER.debug("Data retrieved: {} blocks", this.blocks.size());
          setChanged();

          lvl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
      }

      shouldRetrieve = false;
    }
  }
}
