package net.so_code.seismicexploration.blockentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.so_code.seismicexploration.ModBlockEntities;
import net.so_code.seismicexploration.SeismicExploration;
import net.so_code.seismicexploration.client.ClientLevelDataManager;
import net.so_code.seismicexploration.menu.RecorderMenu;
import net.so_code.seismicexploration.spread.Spread;

public class RecorderBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<BlockPos, Byte> blocks = new HashMap<>();
    private boolean shouldRetrieve = false;

    private int xValue;
    private int zValue;
    private Axis axisValue;

    public RecorderBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.RECORDER_ENTITY.get(), pos, state);
        setSliderValues(pos.getX(), pos.getZ(), Axis.X);
    }

    public void setSliderValues(final int xValue, final int zValue, final Axis axisValue) {
        LOGGER.debug("setSliderValues({}, {}, {})", xValue, zValue, axisValue);
        this.xValue = xValue;
        this.zValue = zValue;
        this.axisValue = axisValue;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_ALL);
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
    @Nullable
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory,
            final Player player) {
        ContainerLevelAccess access;
        if (level != null) {
            access = ContainerLevelAccess.create(level, worldPosition);
        } else {
            access = ContainerLevelAccess.NULL;
        }
        return new RecorderMenu(containerId, playerInventory, access);
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
        LOGGER.debug("loadAdditional - {}", level.isClientSide() ? "client" : "server");
        super.loadAdditional(tag, registry);

        final CompoundTag compound = tag.getCompoundOrEmpty(SeismicExploration.MODID);

        xValue = compound.getIntOr("xValue", worldPosition.getX());
        zValue = compound.getIntOr("zValue", worldPosition.getZ());
        axisValue = Axis.VALUES[compound.getIntOr("axisValue", Axis.X.ordinal())];

        blocks.clear();
        if (compound.contains("blocks")) {
            final ListTag blocksList = compound.getListOrEmpty("blocks");
            for (int i = 0; i < blocksList.size(); i++) {
                final CompoundTag blockTag = blocksList.getCompoundOrEmpty(i);
                final Optional<Integer> x = blockTag.getInt("x");
                final Optional<Integer> y = blockTag.getInt("y");
                final Optional<Integer> z = blockTag.getInt("z");
                final Optional<Byte> color = blockTag.getByte("color");
                if (!x.isPresent() || !y.isPresent() || !z.isPresent() || !color.isPresent()) {
                    LOGGER.warn("Invalid block tag");
                    continue;
                }
                final BlockPos pos = new BlockPos(x.get(), y.get(), z.get());
                blocks.put(pos, color.get());
            }
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registry) {
        LOGGER.debug("saveAdditional - {}", level.isClientSide() ? "client" : "server");
        super.saveAdditional(tag, registry);

        final CompoundTag compound = new CompoundTag();

        compound.putInt("xValue", xValue);
        compound.putInt("zValue", zValue);
        compound.putInt("axisValue", axisValue.ordinal());

        final ListTag blocksList = new ListTag();
        for (final Map.Entry<BlockPos, Byte> entry : blocks.entrySet()) {
            final CompoundTag blockTag = new CompoundTag();
            final BlockPos pos = entry.getKey();
            blockTag.putInt("x", pos.getX());
            blockTag.putInt("y", pos.getY());
            blockTag.putInt("z", pos.getZ());
            blockTag.putInt("color", entry.getValue());
            blocksList.add(blockTag);
        }
        compound.put("blocks", blocksList);

        tag.put(SeismicExploration.MODID, compound);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@Nullable final Connection connection,
            @Nullable final ClientboundBlockEntityDataPacket pkt, @Nullable final Provider lookup) {
        super.onDataPacket(connection, pkt, lookup);
        LOGGER.debug("onDataPacket: {} - received {} blocks", pkt, this.blocks.size());
        ClientLevelDataManager.get().setBlocks(this.blocks);
        ClientLevelDataManager.get().setRecorderParameters(xValue, zValue, axisValue);
    }

    @Override
    public CompoundTag getUpdateTag(final Provider registries) {
        final CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void tick() {
        if (shouldRetrieve) {
            if (level != null) {
                final Level lvl = level;

                if (lvl instanceof final ServerLevel serverLevel) {
                    final Set<BlockPos> placedSensors =
                            Spread.getSpread(serverLevel).getPlacedSensors();
                    this.blocks = placedSensors.stream() //
                            .map(pos -> (SensorBlockEntity) serverLevel.getBlockEntity(pos)) //
                            .map(sensor -> sensor.getBlocks()) //
                            .flatMap(map -> map.entrySet().stream()) //
                            .collect(Collectors.toMap(Map.Entry::getKey, //
                                    entry -> entry.getValue().getPackedId(Brightness.NORMAL), //
                                    (existing, replacement) -> existing // keep the first value
                                                                        // found
                            ));
                    LOGGER.debug("Data retrieved: {} blocks", this.blocks.size());
                    setChanged();

                    lvl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                            Block.UPDATE_ALL);
                }
            }

            shouldRetrieve = false;
        }
    }
}
