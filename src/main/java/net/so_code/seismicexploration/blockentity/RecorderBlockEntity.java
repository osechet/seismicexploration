package net.so_code.seismicexploration.blockentity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.so_code.seismicexploration.ModBlockEntities;
import net.so_code.seismicexploration.menu.RecorderMenu;

public class RecorderBlockEntity extends BlockEntity implements MenuProvider {

    public RecorderBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.RECORDER_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        // No need to use a translatable since the text ine never displayed
        return Component.literal("Recorder");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory,
            final Player player) {
        return new RecorderMenu(containerId, playerInventory,
                ContainerLevelAccess.create(level, worldPosition));
    }
}
