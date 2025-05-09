package net.so_code.seismicexploration.menu;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.so_code.seismicexploration.ModBlocks;
import net.so_code.seismicexploration.ModMenus;

public class RecorderMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;

    public RecorderMenu(final int containerId, final Inventory inv) {
        this(containerId, inv, ContainerLevelAccess.NULL);
    }

    public RecorderMenu(final int containerId, final Inventory inv,
            final ContainerLevelAccess access) {
        super(ModMenus.RECORDER_MENU.get(), containerId);
        this.access = access;
    }

    @Override
    public boolean stillValid(@Nonnull final Player player) {
        return stillValid(this.access, player, ModBlocks.RECORDER.get());
    }

    @Override
    public ItemStack quickMoveStack(@Nonnull final Player player, final int index) {
        return ItemStack.EMPTY;
    }
}
