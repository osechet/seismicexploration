package net.so_coretech.seismicexploration.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.so_coretech.seismicexploration.ModBlocks;
import net.so_coretech.seismicexploration.ModMenus;

public class RecorderMenu extends AbstractContainerMenu {

  private final ContainerLevelAccess access;

  /**
   * This constructor is called on the client side when yje order to receive the menu is received.
   *
   * @param containerId N/A
   * @param inv N/A
   */
  public RecorderMenu(final int containerId, final Inventory inv) {
    this(containerId, inv, ContainerLevelAccess.NULL);
  }

  /**
   * This constructor is called on the server side when the player opens the menu.
   *
   * @param containerId N/A
   * @param inv N/A
   * @param access N/A
   */
  public RecorderMenu(
      final int containerId, final Inventory inv, final ContainerLevelAccess access) {
    super(ModMenus.RECORDER_MENU.get(), containerId);
    this.access = access;
  }

  @Override
  public boolean stillValid(final Player player) {
    return stillValid(this.access, player, ModBlocks.RECORDER.get());
  }

  @Override
  public ItemStack quickMoveStack(final Player player, final int index) {
    return ItemStack.EMPTY;
  }
}
