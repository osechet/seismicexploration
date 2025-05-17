package net.so_coretech.seismicexploration.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;

import java.util.List;

public class FollowMePage extends Page {

    public FollowMePage(final WorkerOrderScreen screen) {
        super("Follow Me", screen);
    }

    @Override
    public List<AbstractWidget> getWidgets() {
        return List.of();
    }

    @Override
    public OrderType getOrderType() {
        return OrderType.FOLLOW_ME;
    }
}
