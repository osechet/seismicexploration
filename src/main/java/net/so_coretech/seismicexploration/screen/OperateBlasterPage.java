package net.so_coretech.seismicexploration.screen;

import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

public class OperateBlasterPage extends Page {

  public OperateBlasterPage(final WorkerOrderScreen screen) {
    super("Operate Blaster", screen);
  }

  @Override
  public List<AbstractWidget> getWidgets() {
    return List.of();
  }

  @Override
  public OrderType getOrderType() {
    return OrderType.OPERATE_BLASTER;
  }
}
