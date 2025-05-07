package net.so_code.seismicexploration.blockentity;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.so_code.seismicexploration.ModBlockEntities;
import net.so_code.seismicexploration.SeismicExploration;

public class BoomBoxBlockEntity extends BlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    private boolean powered = false;

    public BoomBoxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BOOM_BOX_ENTITY.get(), pos, blockState);
    }

    public void switchPower() {
        powered = !powered;
        setChanged();
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registry) {
        LOGGER.info("loadAdditional");
        super.loadAdditional(tag, registry);

        CompoundTag compound = tag.getCompoundOrEmpty(SeismicExploration.MODID);
        this.powered = compound.getBooleanOr("powered", false);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registry) {
        LOGGER.info("saveAdditional");
        super.saveAdditional(tag, registry);

        CompoundTag compound = new CompoundTag();
        compound.putBoolean("powered", this.powered);
        tag.put(SeismicExploration.MODID, compound);
    }
}
