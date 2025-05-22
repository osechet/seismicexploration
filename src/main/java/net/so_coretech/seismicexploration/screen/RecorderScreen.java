package net.so_coretech.seismicexploration.screen;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.so_coretech.seismicexploration.SeismicExploration;
import net.so_coretech.seismicexploration.client.ClientLevelDataManager;
import net.so_coretech.seismicexploration.menu.RecorderMenu;
import net.so_coretech.seismicexploration.network.RecorderPositionPacket;
import net.so_coretech.seismicexploration.network.RecorderScreenValuesPacket;
import net.so_coretech.seismicexploration.spread.SliceData;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class RecorderScreen extends AbstractContainerScreen<RecorderMenu> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int AXIS_X = 0;
    public static final int AXIS_Z = 1;

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SeismicExploration.MODID, "textures/gui/recorder/recorder_gui.png");
    private static final int GUI_TEXTURE_WIDTH = 256;
    private static final int GUI_TEXTURE_HEIGHT = 174;

    // The recorder position is normally sent by the server before opening the screen.
    private static @Nullable BlockPos recorderPos;
    private @Nullable CustomSlider xCoordinateField;
    private @Nullable CustomSlider zCoordinateField;
    private @Nullable CustomSlider axisField;
    private @Nullable SliceInstance sliceInstance;

    /**
     * Used to set the position of the recorder used to display the screen. The position is sent by
     * the server before opening the screen.
     *
     * @param recorderPos the position of the block used to open this screen.
     * @see RecorderPositionPacket
     */
    public static void setRecorderPosition(final BlockPos recorderPos) {
        LOGGER.debug("setRecorderPosition({})", recorderPos);
        RecorderScreen.recorderPos = recorderPos;
    }

    public RecorderScreen(final RecorderMenu menu, final Inventory inv, final Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        imageWidth = GUI_TEXTURE_WIDTH;
        imageHeight = GUI_TEXTURE_HEIGHT;

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        final int contentX = x + 6;
        final int contentY = y + 6;

        // The first time we use the block, we use the block's position. Later we use the latest input values
        final ClientLevelDataManager dm = ClientLevelDataManager.get();
        final int xValue = dm.getCenterX().orElse(Objects.requireNonNull(recorderPos).getX());
        final int zValue = dm.getCenterZ().orElse(recorderPos.getZ());
        final int axisValue = dm.getAxis().orElse(AXIS_X);

        // Initialize coordinate input fields with the block's position
        xCoordinateField = new CustomSlider(contentX + 10, contentY + 10, 60, 20,
                Component.literal("x: "), Component.literal(""), xValue - 64, xValue + 64, xValue,
                1, 0, true, this::sendValuesToServer);
        zCoordinateField = new CustomSlider(contentX + 10, contentY + 35, 60, 20,
                Component.literal("z: "), Component.literal(""), zValue - 64, zValue + 64, zValue,
                1, 0, true, this::sendValuesToServer);
        axisField = new CustomSlider(contentX + 10, contentY + 60, 60, 20,
                SeismicExploration.translatable("slider", "recorder_axis"), Component.literal(""),
                0, 1, axisValue, 1, 0, true, this::sendValuesToServer,
                value -> value == 0 ? "X" : "Z");

        addRenderableWidget(xCoordinateField);
        addRenderableWidget(zCoordinateField);
        addRenderableWidget(axisField);

        this.sliceInstance = new SliceInstance();
    }

    @Override
    public void onClose() {
        if (sliceInstance != null) {
            sliceInstance.close();
        }

        super.onClose();
    }

    private void sendValuesToServer() {
        LOGGER.debug("sendValuesToServer");
        PacketDistributor.sendToServer(
                new RecorderScreenValuesPacket(
                        Objects.requireNonNull(xCoordinateField).getValueInt(),
                        Objects.requireNonNull(zCoordinateField).getValueInt(),
                        Objects.requireNonNull(axisField).getValueInt(),
                        Objects.requireNonNull(recorderPos)));
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
                       final float f) {
        super.render(guiGraphics, mouseX, mouseY, f);

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        Objects.requireNonNull(xCoordinateField).render(guiGraphics, mouseX, mouseY, f);
        Objects.requireNonNull(zCoordinateField).render(guiGraphics, mouseX, mouseY, f);
        Objects.requireNonNull(axisField).render(guiGraphics, mouseX, mouseY, f);

        final int monitorX = 89;
        final int monitorY = 7;
        final int monitorWidth = 160;
        final int monitorHeight = 160;

        final SliceData savedData = ClientLevelDataManager.get().getSlice();
        Objects.requireNonNull(sliceInstance).update(savedData);

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
}
