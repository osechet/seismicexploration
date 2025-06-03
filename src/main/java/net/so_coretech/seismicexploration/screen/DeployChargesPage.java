package net.so_coretech.seismicexploration.screen;

import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

public class DeployChargesPage extends Page {

  public DeployChargesPage(final WorkerOrderScreen screen) {
    super("Deploy charges", screen);
  }

  @Override
  public List<AbstractWidget> getWidgets() {
    return List.of();
  }

  @Override
  public OrderType getOrderType() {
    return OrderType.DEPLOY_CHARGES;
  }
}
