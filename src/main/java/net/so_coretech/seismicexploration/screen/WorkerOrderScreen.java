package net.so_coretech.seismicexploration.screen;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.so_coretech.seismicexploration.ModNetworking;
import net.so_coretech.seismicexploration.entity.ai.goal.OrderType;
import net.so_coretech.seismicexploration.network.DeploySensorsOrderPacket;
import net.so_coretech.seismicexploration.network.OnCloseWorkerOrderMenuPacket;
import net.so_coretech.seismicexploration.network.WorkerOrdersPacket;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class WorkerOrderScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int imageWidth = 256;
    private final int imageHeight = 256;

    private final int entityId;
    private final BlockPos playerPos;

    private int pageIndex = 0;
    private final List<Page> pages = new ArrayList<>();
    private @Nullable CustomSlider pageSlider;
    private @Nullable Button applyButton;


    public WorkerOrderScreen(final int entityId, final BlockPos playerPos) {
        super(Component.literal(""));
        this.entityId = entityId;
        this.playerPos = playerPos;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();

        // Notify the server the screen has been closed
        ModNetworking.sendToServer(new OnCloseWorkerOrderMenuPacket(entityId));
    }

    private void setCurrentPage(final int pageIndex) {
        this.removeWidgets(pages.get(this.pageIndex).getWidgets());

        this.pageIndex = pageIndex;
        this.addRenderableWidgets(pages.get(this.pageIndex).getWidgets());
    }

    private void addRenderableWidgets(final List<AbstractWidget> widgets) {
        for (final AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }

    private void removeWidgets(final List<AbstractWidget> widgets) {
        for (final AbstractWidget widget : widgets) {
            this.removeWidget(widget);
        }
    }

    private void sendOrder() {
        LOGGER.debug("sendOrder");
        final Object packet;
        final OrderType orderType = pages.get(pageIndex).getOrderType();
        switch (orderType) {
            case FOLLOW_ME -> packet = new WorkerOrdersPacket(entityId, orderType);
            case FREE_ROAMING -> packet = new WorkerOrdersPacket(entityId, orderType);
            case DEPLOY_SENSORS -> {
                final DeploySensorsPage page = (DeploySensorsPage) pages.get(pageIndex);
                packet = new DeploySensorsOrderPacket(entityId,
                    page.getStartPos(), page.getDirection(),
                    page.getCount(), page.getGap());
            }
            case DEPLOY_CHARGES -> packet = new WorkerOrdersPacket(entityId, orderType);
            case OPERATE_BOOM_BOX -> packet = new WorkerOrdersPacket(entityId, orderType);
            default -> {
                LOGGER.error("Unknown order type: {}", orderType);
                return;
            }
        }
        ModNetworking.sendToServer(packet);
    }

    @Override
    protected void init() {
        super.init();

        final int left = (this.width - imageWidth) / 2;
        final int top = (this.height - imageHeight) / 2;

        // Create pages
        pages.add(new FollowMePage(this));
        pages.add(new FreeRoamingPage(this));
        pages.add(new DeploySensorsPage(this, left, top + 10 + this.font.lineHeight + 5 + 20,
            playerPos.getX(), playerPos.getZ()));
        pages.add(new DeployChargesPage(this));
        pages.add(new OperateBoomBoxPage(this));

        // Slider for pages: 0, 1, 2
        pageSlider = new CustomSlider(
            left + 10, top + 10 + this.font.lineHeight + 5,
            this.imageWidth - 20, 20,
            Component.literal(""), Component.literal(""),
            0, pages.size() - 1, 0, 0, pageIndex, true,
            () -> setCurrentPage(Objects.requireNonNull(pageSlider).getValueInt()),
            value -> pages.get(value).getTitle()
        );

        // Apply button
        final int buttonWidth = 60;
        final int buttonHeight = 20;
        applyButton = Button.builder(Component.literal("Apply"), btn -> {
                                // Send the current order to the server
                                sendOrder();
                                // Close the screen
                                onClose();
                            })
                            .bounds(left + imageWidth - buttonWidth - 10, top + imageHeight - buttonHeight - 10, buttonWidth, buttonHeight)
                            .build();

        addRenderableWidget(pageSlider);
        addRenderableWidget(applyButton);

        setCurrentPage(pageIndex);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
                       final float f) {
        super.render(guiGraphics, mouseX, mouseY, f);

        final int left = (this.width - imageWidth) / 2;
        final int top = (this.height - imageHeight) / 2;

        final String label = "How can I help?";
        guiGraphics.drawString(this.font, label, left + 10, top + 10, 0xFFFFFF);

        if (pageSlider != null) {
            pageSlider.render(guiGraphics, mouseX, mouseY, f);
        }
        if (applyButton != null) {
            applyButton.render(guiGraphics, mouseX, mouseY, f);
        }

        pages.get(pageIndex).render(guiGraphics, mouseX, mouseY, f, left, top + 10 + this.font.lineHeight + 5 + 20);

        for (final AbstractWidget widget : pages.get(pageIndex).getWidgets()) {
            widget.render(guiGraphics, mouseX, mouseY, f);
        }
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
                                 final float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0x48c0c0c0);
    }
}
