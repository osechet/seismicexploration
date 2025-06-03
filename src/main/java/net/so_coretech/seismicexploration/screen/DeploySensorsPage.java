package net.so_coretech.seismicexploration.screen;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

public class DeploySensorsPage extends Page {

  private final EditBox startXField;
  private final EditBox startZField;
  private final EditBox countField;
  private final EditBox gapField;
  private final CustomSlider directionField;

  protected DeploySensorsPage(
      final WorkerOrderScreen screen,
      final int left,
      final int top,
      final int initialX,
      final int initialZ) {
    super("Deploy sensors", screen);

    directionField =
        new CustomSlider(
            left + 10,
            top + 5,
            60,
            20,
            Component.literal(""),
            Component.literal(""),
            Direction.NORTH.ordinal(),
            Direction.EAST.ordinal(),
            Direction.NORTH.ordinal(),
            1,
            0,
            true,
            null,
            value -> Direction.values()[value].toString());
    startXField =
        new EditBox(
            screen.getFont(), left + 10, top + 5 + 20 + 5, 60, 20, Component.literal("start"));
    startXField.setValue(Integer.toString(initialX));
    startZField =
        new EditBox(
            screen.getFont(),
            left + 10 + 60 + 5,
            top + 5 + 20 + 5,
            60,
            20,
            Component.literal("start"));
    startZField.setValue(Integer.toString(initialZ));
    countField =
        new EditBox(
            screen.getFont(),
            left + 10,
            top + 5 + 20 + 5 + 20 + 5,
            60,
            20,
            Component.literal("end"));
    countField.setValue("50");
    gapField =
        new EditBox(
            screen.getFont(),
            left + 10,
            top + 5 + 20 + 5 + 20 + 5 + 20 + 5,
            60,
            20,
            Component.literal("end"));
    gapField.setValue("3");
  }

  public BlockPos getStartPos() {
    final int x = Integer.parseInt(startXField.getValue());
    final int y = 0;
    final int z = Integer.parseInt(startZField.getValue());
    return new BlockPos(x, y, z);
  }

  public Direction getDirection() {
    return Direction.values()[directionField.getValueInt()];
  }

  public int getCount() {
    return Integer.parseInt(countField.getValue());
  }

  public int getGap() {
    return Integer.parseInt(gapField.getValue());
  }

  @Override
  public List<AbstractWidget> getWidgets() {
    return List.of(directionField, startXField, startZField, directionField, countField, gapField);
  }

  @Override
  public OrderType getOrderType() {
    return OrderType.DEPLOY_SENSORS;
  }

  @Override
  public void render(
      final GuiGraphics guiGraphics,
      final int mouseX,
      final int mouseY,
      final float f,
      final int left,
      final int top) {
    layout(
        guiGraphics,
        new Object[][] {
          {"Start", startXField, startZField},
          {"Direction", directionField},
          {"Count", countField},
          {"Gap", gapField},
        },
        left,
        top);
  }

  private void layout(
      final GuiGraphics guiGraphics, final Object[][] widgets, final int left, final int top) {
    final int[] colX = {
      left + 10, left + 10 + 50, left + 10 + 50 + 65,
    };
    final int[] rowY = {
      top + 5, top + 5 + 25, top + 5 + 25 + 25, top + 5 + 25 + 25 + 25,
    };
    final int fontHeight = screen.getFont().lineHeight;
    final int widgetHeight = 20; // Default height for widgets

    for (int r = 0; r < widgets.length; r++) {
      final Object[] row = widgets[r];
      for (int c = 1; c < row.length; c++) {
        if (row[c] instanceof final AbstractWidget widget) {
          widget.setX(colX[c]);
          widget.setY(rowY[r]);
        }
      }

      final String label = (String) row[0];
      final int labelY = rowY[r] + (widgetHeight - fontHeight) / 2;
      guiGraphics.drawString(screen.getFont(), label, colX[0], labelY, 0xFFFFFF);
    }
  }
}
