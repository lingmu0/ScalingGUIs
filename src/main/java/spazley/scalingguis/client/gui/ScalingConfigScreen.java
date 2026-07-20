package spazley.scalingguis.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import spazley.scalingguis.client.ScaleController;
import spazley.scalingguis.config.ConfigManager;
import spazley.scalingguis.config.CustomScales;

public final class ScalingConfigScreen extends Screen {
    private final Screen parent;
    private final CustomScales config = ConfigManager.get();

    public ScalingConfigScreen(Screen parent) {
        super(Component.translatable("scalingguis.config.main.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int sliderWidth = Math.min(310, width - 40);
        int x = center - sliderWidth / 2;
        int y = Math.max(40, height / 2 - 92);

        addRenderableWidget(new ScaleSlider(x, y, sliderWidth,
                Component.translatable("scalingguis.config.main.guiscale"),
                Component.translatable("scalingguis.config.main.guiscale.tooltip"),
                config.guiScale, false, value -> config.guiScale = value));
        addRenderableWidget(new ScaleSlider(x, y + 24, sliderWidth,
                Component.translatable("scalingguis.config.main.hudscale"),
                Component.translatable("scalingguis.config.main.hudscale.tooltip"),
                config.hudScale, true, value -> config.hudScale = value));
        addRenderableWidget(new ScaleSlider(x, y + 48, sliderWidth,
                Component.translatable("scalingguis.config.main.tooltipscale"),
                Component.translatable("scalingguis.config.main.tooltipscale.tooltip"),
                config.tooltipScale, true, value -> config.tooltipScale = value));

        int half = (sliderWidth - 4) / 2;
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.individual.title"),
                        button -> minecraft.setScreen(new ClassScaleScreen(this, false)))
                .bounds(x, y + 78, half, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.group.title"),
                        button -> minecraft.setScreen(new ClassScaleScreen(this, true)))
                .bounds(x + half + 4, y + 78, half, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.blacklist.title"),
                        button -> minecraft.setScreen(new StringSetScreen(this, StringSetScreen.Kind.BLACKLIST)))
                .bounds(x, y + 102, half, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.dynamics.title"),
                        button -> minecraft.setScreen(new StringSetScreen(this, StringSetScreen.Kind.DYNAMIC)))
                .bounds(x + half + 4, y + 102, half, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.general.title"),
                        button -> minecraft.setScreen(new GeneralSettingsScreen(this)))
                .bounds(x, y + 126, sliderWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> finish())
                .bounds(center - 100, height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        finish();
    }

    private void finish() {
        ConfigManager.save();
        ScaleController.refreshCurrentScale();
        minecraft.setScreen(parent);
    }
}
