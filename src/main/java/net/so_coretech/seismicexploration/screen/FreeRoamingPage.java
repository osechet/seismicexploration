package net.so_coretech.seismicexploration.screen;

import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

public class FreeRoamingPage extends Page {

  public FreeRoamingPage(final WorkerOrderScreen screen) {
    super("Free roaming", screen);
  }

  @Override
  public List<AbstractWidget> getWidgets() {
    return List.of();
  }

  @Override
  public OrderType getOrderType() {
    return OrderType.FREE_ROAMING;
  }
}
