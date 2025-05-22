package net.so_coretech.seismicexploration.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * The CustomSlider class extends ExtendedSlider to easily manage formatter.
 */
public class CustomSlider extends ExtendedSlider {

    private final @Nullable CustomSlider.Action onApply;
    private final @Nullable Function<Integer, String> customFormat;

    public CustomSlider(final int x, final int y, final int width, final int height,
                        final Component prefix, final Component suffix, final double minValue,
                        final double maxValue, final double currentValue, final double stepSize,
                        final int precision, final boolean drawString,
                        @Nullable final CustomSlider.Action onApply) {
        this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize,
                precision, drawString, onApply, null);
    }

    public CustomSlider(final int x, final int y, final int width, final int height,
                        final Component prefix, final Component suffix, final double minValue,
                        final double maxValue, final double currentValue, final double stepSize,
                        final int precision, final boolean drawString,
                        @Nullable final CustomSlider.Action onApply,
                        @Nullable final Function<Integer, String> customFormat) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize,
                precision, drawString);
        this.onApply = onApply;
        this.customFormat = customFormat;
        this.updateMessage();
    }

    /*
     * Workaround to make the slider call applyValue() when using the keyboard.
     */
    @Override
    public void setValue(final double value) {
        final double oldValue = this.value;
        super.setValue(value);
        if (!Mth.equal(oldValue, this.value))
            this.applyValue();
    }

    @Override
    protected void applyValue() {
        if (onApply != null) {
            this.onApply.execute();
        }
    }

    @Override
    public String getValueString() {
        if (this.customFormat == null) {
            return super.getValueString();
        }
        return this.customFormat.apply(this.getValueInt());
    }

    public interface Action {
        void execute();
    }
}
