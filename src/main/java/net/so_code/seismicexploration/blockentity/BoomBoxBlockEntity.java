package net.so_code.seismicexploration.blockentity;

import java.util.Set;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.so_code.seismicexploration.ModBlockEntities;
import net.so_code.seismicexploration.block.BoomBoxBlock;
import net.so_code.seismicexploration.spread.Spread;

public class BoomBoxBlockEntity extends BlockEntity implements TickableBlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    private int tickCount = 0;
    public final static int ticksPerCycle = 20;
    public final static int cyclesCount = 5;

    public BoomBoxBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.BOOM_BOX_ENTITY.get(), pos, state);
    }

    public boolean isPowered() {
        return getBlockState().getValue(BoomBoxBlock.POWERED);
    }

    public void switchPower() {
        if (level != null) {
            final Level lvl = level;
            final boolean powered = !getBlockState().getValue(BoomBoxBlock.POWERED);
            lvl.setBlock(worldPosition, getBlockState().setValue(BoomBoxBlock.POWERED, powered),
                    Block.UPDATE_CLIENTS);
            lvl.setBlock(worldPosition, getBlockState().setValue(BoomBoxBlock.WORKING, false),
                    Block.UPDATE_CLIENTS);

            if (powered && lvl instanceof final ServerLevel serverLevel) {
                LOGGER.debug("Boom box firing shot at {}", worldPosition);
                final Set<BlockPos> positions = Spread.getSpread(serverLevel).getPlacedSensors();
                for (final BlockPos sensorPos : positions) {
                    final BlockEntity be = lvl.getBlockEntity(sensorPos);
                    if (be instanceof final SensorBlockEntity blockEntity) {
                        final int midX = (worldPosition.getX() + sensorPos.getX()) / 2;
                        final int midZ = (worldPosition.getZ() + sensorPos.getZ()) / 2;
                        final int y = lvl.getMinY();
                        final BlockPos pos = new BlockPos(midX, y, midZ);
                        blockEntity.startRecording(pos);
                    }
                }
            }
        }
    }

    public void poweroff() {
        if (level != null) {
            final Level lvl = level;
            lvl.setBlock(worldPosition, getBlockState().setValue(BoomBoxBlock.POWERED, false),
                    Block.UPDATE_CLIENTS);
            lvl.setBlock(worldPosition, getBlockState().setValue(BoomBoxBlock.WORKING, false),
                    Block.UPDATE_CLIENTS);
            tickCount = 0;
        }
    }

    /*
     * switchWorking changes the working state in order to make the boom box' screen blink.
     */
    @SuppressWarnings("null")
    private void switchWorking() {
        if (level != null) {
            final boolean working = getBlockState().getValue(BoomBoxBlock.WORKING);
            level.setBlock(worldPosition, getBlockState().setValue(BoomBoxBlock.WORKING, !working),
                    Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void tick() {
        if (isPowered()) {
            tickCount++;
            if (tickCount >= cyclesCount * ticksPerCycle) {
                LOGGER.debug("Working cycles terminated. Powering off.");
                poweroff();
            } else if (tickCount % ticksPerCycle == 0) {
                LOGGER.debug("Cycle terminated.");
                switchWorking();
            }
        }
    }

}
