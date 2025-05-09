package net.so_code.seismicexploration.screen;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.so_code.seismicexploration.menu.RecorderMenu;

public class RecorderScreen extends AbstractContainerScreen<RecorderMenu> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int BACKGROUND = 0xffb4b4b4;
    private static final int MONITOR = 0xffbedaf6;
    private static final int HIGHLIGHT = 0xc0afafaf;
    private static final int SHADOW = 0xc05c5c5c;

    private final int margin = 10;

    private final BlockPos playerPos;
    private ForgeSlider xCoordinateField;
    private ForgeSlider zCoordinateField;

    public RecorderScreen(final RecorderMenu menu, final Inventory inv, final Component title) {
        super(menu, inv, title);
        playerPos = inv.player.blockPosition();
    }

    @Override
    protected void init() {
        super.init();
        imageWidth = width - 2 * margin;
        imageHeight = height - 2 * margin;

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        // Retrieve the current block's position
        final int blockX = playerPos.getX();
        final int blockZ = playerPos.getZ();

        // Initialize coordinate input fields with the block's position
        xCoordinateField = new CustomSlider(x + 10, y + 10, 50, 20, Component.literal("x: "),
                Component.literal(""), blockX - 50, blockX + 50, blockX, 1, 0, true,
                value -> LOGGER.info("X changed: {}", value));

        zCoordinateField = new CustomSlider(x + 10, y + 40, 50, 20, Component.literal("z: "),
                Component.literal(""), blockZ - 50, blockZ + 50, blockZ, 1, 0, true,
                value -> LOGGER.info("Y changed: {}", value));

        addRenderableWidget(xCoordinateField);
        addRenderableWidget(zCoordinateField);
    }

    @Override
    public void render(@Nonnull final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float f) {
        super.render(guiGraphics, mouseX, mouseY, f);

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND);
        xCoordinateField.render(guiGraphics, mouseX, mouseY, f);
        zCoordinateField.render(guiGraphics, mouseX, mouseY, f);

        final int monitorX = x + margin + 50 + margin;
        final int monitorY = y + margin;
        final int monitorWidth = imageWidth - (margin + 50 + margin + margin);
        final int monitorHeight = imageHeight - (2 * margin);
        guiGraphics.hLine(monitorX, monitorX + monitorWidth, monitorY, SHADOW);
        guiGraphics.vLine(monitorX, monitorY, monitorY + monitorHeight, SHADOW);
        guiGraphics.hLine(monitorX, monitorX + monitorWidth, monitorY + monitorHeight, HIGHLIGHT);
        guiGraphics.vLine(monitorX + monitorWidth, monitorY, monitorY + monitorHeight, HIGHLIGHT);
        guiGraphics.fill(monitorX + 1, monitorY + 1, monitorX + monitorWidth,
                monitorY + monitorHeight, MONITOR);
    }

    @Override
    protected void renderBg(@Nonnull final GuiGraphics guiGraphics, final float partialTicks,
            final int mouseX, final int mouseY) {
        // no op
    }

    @Override
    protected void renderLabels(@Nonnull final GuiGraphics guiGraphics, final int mouseX,
            final int mouseY) {
        // Override to prevent rendering the "Inventory" text
    }

    /**
     * Reimplement method as in ContainerEventHandler since AbstractContainerScreen stops forwarding
     * the event to the widgets.
     */
    @Override
    public boolean mouseDragged(final double pMouseX, final double pMouseY, final int pButton,
            final double pDragX, final double pDragY) {
        final Optional<GuiEventListener> optional = this.getChildAt(pMouseX, pMouseY);
        if (optional.isEmpty()) {
            return false;
        } else {
            final GuiEventListener guieventlistener = optional.get();
            if (guieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
                this.setFocused(guieventlistener);
                if (pButton == 0) {
                    this.setDragging(true);
                }
            }

            return true;
        }
    }

    /**
     * The CustomSlider class extends ForgeSlider to easily manage change event.
     */
    private final static class CustomSlider extends ForgeSlider {

        public interface ChangeListener {
            public void onValueChanged(int value);
        }

        private final ChangeListener changeListener;

        public CustomSlider(final int x, final int y, final int width, final int height,
                final Component prefix, final Component suffix, final double minValue,
                final double maxValue, final double currentValue, final double stepSize,
                final int precision, final boolean drawString,
                final ChangeListener changeListener) {
            super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize,
                    precision, drawString);
            this.changeListener = changeListener;
        }

        @Override
        protected void applyValue() {
            changeListener.onValueChanged(getValueInt());
        }

    }
}
