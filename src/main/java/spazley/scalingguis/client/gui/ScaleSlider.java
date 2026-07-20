package spazley.scalingguis.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import spazley.scalingguis.config.CustomScales;

import java.util.function.IntConsumer;

final class ScaleSlider extends AbstractSliderButton {
    private final Component label;
    private final int maximum;
    private final IntConsumer setter;
    private int scale;

    ScaleSlider(int x, int y, int width, Component label, Component tooltip, int scale,
                boolean allowMainScale, IntConsumer setter) {
        super(x, y, width, 20, Component.empty(), normalized(scale, allowMainScale));
        this.label = label;
        this.maximum = allowMainScale ? CustomScales.MAIN_GUI_SCALE : CustomScales.MAX_EXPLICIT_SCALE;
        this.setter = setter;
        this.scale = Math.max(0, Math.min(maximum, scale));
        setTooltip(Tooltip.create(tooltip));
        updateMessage();
    }

    int getScale() {
        return scale;
    }

    @Override
    protected void updateMessage() {
        setMessage(label.copy().append(": ").append(scaleName(scale)));
    }

    @Override
    protected void applyValue() {
        int next = (int) Math.round(value * maximum);
        if (next == scale) return;
        scale = next;
        value = maximum == 0 ? 0.0D : (double) scale / maximum;
        setter.accept(scale);
    }

    private static double normalized(int scale, boolean allowMainScale) {
        int max = allowMainScale ? CustomScales.MAIN_GUI_SCALE : CustomScales.MAX_EXPLICIT_SCALE;
        return (double) Math.max(0, Math.min(max, scale)) / max;
    }

    static Component scaleName(int scale) {
        return switch (scale) {
            case 0 -> Component.translatable("scalingguis.slidertext.auto");
            case 1 -> Component.translatable("scalingguis.slidertext.small");
            case 2 -> Component.translatable("scalingguis.slidertext.normal");
            case 3 -> Component.translatable("scalingguis.slidertext.large");
            case CustomScales.MAIN_GUI_SCALE -> Component.translatable("scalingguis.slidertext.default");
            default -> Component.translatable("scalingguis.slidertext.scale", scale);
        };
    }
}
