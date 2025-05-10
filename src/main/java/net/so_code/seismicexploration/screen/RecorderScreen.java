package net.so_code.seismicexploration.screen;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.so_code.seismicexploration.SeismicExploration;
import net.so_code.seismicexploration.menu.RecorderMenu;
import net.so_code.seismicexploration.spread.SliceSavedData;

public class RecorderScreen extends AbstractContainerScreen<RecorderMenu> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SeismicExploration.MODID, "textures/gui/recorder/recorder_gui.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 174;

    private final BlockPos playerPos;
    private final Level level;
    private ForgeSlider xCoordinateField;
    private ForgeSlider zCoordinateField;
    private ForgeSlider axisField;

    public RecorderScreen(final RecorderMenu menu, final Inventory inv, final Component title) {
        super(menu, inv, title);
        playerPos = inv.player.blockPosition();
        level = inv.player.level();
    }

    @Override
    protected void init() {
        super.init();
        imageWidth = GUI_TEXTURE_WIDTH;
        imageHeight = GUI_TEXTURE_HEIGHT;

        // Retrieve the current block's position
        final int blockX = playerPos.getX();
        final int blockZ = playerPos.getZ();

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        final int contentX = x + 6;
        final int contentY = y + 6;

        // Initialize coordinate input fields with the block's position
        xCoordinateField = new CustomSlider(contentX + 10, contentY + 10, 60, 20,
                Component.literal("x: "), Component.literal(""), blockX - 64, blockX + 64, blockX,
                1, 0, true, value -> LOGGER.info("X changed: {}", value));

        zCoordinateField = new CustomSlider(contentX + 10, contentY + 35, 60, 20,
                Component.literal("z: "), Component.literal(""), blockZ - 64, blockZ + 64, blockZ,
                1, 0, true, value -> LOGGER.info("Y changed: {}", value));

        axisField = new CustomSlider(contentX + 10, contentY + 60, 60, 20,
                Component.literal("along "), Component.literal(" axis"), 0, 1, 0, 1, 0, true,
                value -> LOGGER.info("Axis changed: {}", value), value -> value == 0 ? "X" : "Z");

        addRenderableWidget(xCoordinateField);
        addRenderableWidget(zCoordinateField);
        addRenderableWidget(axisField);

        // TODO: SliceSavedData must be created on server side
        final SliceSavedData savedData = new SliceSavedData();
        savedData.update();
        final SliceInstance sliceInstance = new SliceInstance(savedData);
        sliceInstance.update();
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float f) {
        super.render(guiGraphics, mouseX, mouseY, f);

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        xCoordinateField.render(guiGraphics, mouseX, mouseY, f);
        zCoordinateField.render(guiGraphics, mouseX, mouseY, f);
        axisField.render(guiGraphics, mouseX, mouseY, f);

        final int monitorX = 89;
        final int monitorY = 7;
        final int monitorWidth = 160;
        final int monitorHeight = 160;

        // // Draw the spread slice
        // if (level instanceof final ServerLevel serverLevel) {
        // final int centerX = xCoordinateField.getValueInt();
        // final int centerZ = zCoordinateField.getValueInt();
        // LOGGER.debug("Creating slice...");
        // final Spread.Slice slice =
        // Spread.getSpread(serverLevel).getSlice(level, centerX, centerZ, Axis.X);
        // LOGGER.debug("Slice created: {}", slice);
        // }

        final ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(SeismicExploration.MODID, "slice/unique");

        guiGraphics.blit(RenderType::guiTextured, location, x + monitorX, y + monitorY, 0, 0,
                monitorWidth, monitorHeight, 320, 320, 320, 320);
    }


    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTicks,
            final int mouseX, final int mouseY) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, GUI_TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth,
                this.imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
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
        private final Function<Integer, String> customFormat;

        public CustomSlider(final int x, final int y, final int width, final int height,
                final Component prefix, final Component suffix, final double minValue,
                final double maxValue, final double currentValue, final double stepSize,
                final int precision, final boolean drawString,
                final ChangeListener changeListener) {
            this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize,
                    precision, drawString, changeListener, null);
        }

        public CustomSlider(final int x, final int y, final int width, final int height,
                final Component prefix, final Component suffix, final double minValue,
                final double maxValue, final double currentValue, final double stepSize,
                final int precision, final boolean drawString, final ChangeListener changeListener,
                @Nullable final Function<Integer, String> customFormat) {
            super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize,
                    precision, drawString);
            this.changeListener = changeListener;
            this.customFormat = customFormat;
        }

        @Override
        protected void applyValue() {
            changeListener.onValueChanged(getValueInt());
        }

        @Override
        public String getValueString() {
            if (this.customFormat == null) {
                return super.getValueString();
            }
            return this.customFormat.apply(this.getValueInt());
        }
    }
}
