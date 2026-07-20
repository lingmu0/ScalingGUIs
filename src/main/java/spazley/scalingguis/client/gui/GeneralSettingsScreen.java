package spazley.scalingguis.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import spazley.scalingguis.config.ConfigManager;
import spazley.scalingguis.config.CustomScales;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class GeneralSettingsScreen extends Screen {
    private final Screen parent;
    private final CustomScales config = ConfigManager.get();

    GeneralSettingsScreen(Screen parent) {
        super(Component.translatable("scalingguis.config.general.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 155;
        int y = Math.max(45, height / 2 - 70);
        int w = 310;
        addToggle(x, y, w, "scalingguis.config.general.log", () -> config.logGuiClassNames,
                value -> config.logGuiClassNames = value);
        addToggle(x, y + 24, w, "scalingguis.config.general.chat", () -> config.logGuiClassNamesChat,
                value -> config.logGuiClassNamesChat = value);
        addToggle(x, y + 48, w, "scalingguis.config.general.persistent", () -> config.persistentLog,
                value -> config.persistentLog = value);
        addToggle(x, y + 72, w, "scalingguis.config.general.sort", () -> config.sortLoggedAlphabetically,
                value -> config.sortLoggedAlphabetically = value);
        addToggle(x, y + 96, w, "scalingguis.config.general.defaults", () -> config.addDefaultBlacklist,
                value -> config.addDefaultBlacklist = value);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> done())
                .bounds(width / 2 - 100, height - 28, 200, 20).build());
    }

    private void addToggle(int x, int y, int width, String key, BooleanSupplier getter,
                           Consumer<Boolean> setter) {
        Button[] holder = new Button[1];
        holder[0] = Button.builder(toggleLabel(key, getter.getAsBoolean()), button -> {
                    setter.accept(!getter.getAsBoolean());
                    holder[0].setMessage(toggleLabel(key, getter.getAsBoolean()));
                }).bounds(x, y, width, 20).build();
        addRenderableWidget(holder[0]);
    }

    private static Component toggleLabel(String key, boolean enabled) {
        return Component.translatable(key).append(": ")
                .append(Component.translatable(enabled ? "options.on" : "options.off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        done();
    }

    private void done() {
        ConfigManager.save();
        minecraft.setScreen(parent);
    }
}
