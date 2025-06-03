package net.so_coretech.seismicexploration.screen;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

public abstract class Page {

  private final String title;
  protected final Screen screen;

  protected Page(final String title, final Screen screen) {
    this.title = title;
    this.screen = screen;
  }

  public String getTitle() {
    return title;
  }

  public abstract List<AbstractWidget> getWidgets();

  public abstract OrderType getOrderType();

  public void render(
      final GuiGraphics guiGraphics,
      final int mouseX,
      final int mouseY,
      final float f,
      final int left,
      final int top) {}
}
