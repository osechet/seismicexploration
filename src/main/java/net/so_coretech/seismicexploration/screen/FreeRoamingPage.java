package net.so_coretech.seismicexploration.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

import java.util.List;

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
