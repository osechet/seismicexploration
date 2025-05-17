package net.so_coretech.seismicexploration.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

import java.util.List;

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
