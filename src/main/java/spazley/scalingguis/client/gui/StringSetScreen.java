package spazley.scalingguis.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import spazley.scalingguis.config.ConfigManager;
import spazley.scalingguis.config.CustomScales;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class StringSetScreen extends Screen {
    enum Kind {
        BLACKLIST("scalingguis.config.blacklist.title"),
        DYNAMIC("scalingguis.config.dynamics.title");

        private final String titleKey;

        Kind(String titleKey) {
            this.titleKey = titleKey;
        }
    }

    private final Screen parent;
    private final Kind kind;
    private final CustomScales config = ConfigManager.get();
    private EditBox input;
    private Component error = Component.empty();
    private int page;
    private int pageCount = 1;

    StringSetScreen(Screen parent, Kind kind) {
        super(Component.translatable(kind.titleKey));
        this.parent = parent;
        this.kind = kind;
    }

    @Override
    protected void init() {
        List<String> values = new ArrayList<>(values());
        values.sort(String.CASE_INSENSITIVE_ORDER);
        int pageSize = Math.max(1, (height - 130) / 24);
        pageCount = Math.max(1, (values.size() + pageSize - 1) / pageSize);
        page = Math.max(0, Math.min(page, pageCount - 1));
        int from = page * pageSize;
        int to = Math.min(values.size(), from + pageSize);
        int rowWidth = Math.min(400, width - 20);
        int x = width / 2 - rowWidth / 2;
        int y = 38;

        for (String value : values.subList(from, to)) {
            addRenderableWidget(Button.builder(Component.literal(shorten(value, 56)), button -> { })
                    .bounds(x, y, rowWidth - 44, 20)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(value)))
                    .build());
            addRenderableWidget(Button.builder(Component.literal("×"), button -> {
                        values().remove(value);
                        ConfigManager.save();
                        rebuildWidgets();
                    }).bounds(x + rowWidth - 40, y, 40, 20).build());
            y += 24;
        }

        int inputY = height - 76;
        input = new EditBox(font, x, inputY, rowWidth - 64, 20,
                Component.translatable("scalingguis.config.add.classname"));
        input.setHint(Component.translatable("scalingguis.config.add.classname"));
        input.setMaxLength(512);
        addRenderableWidget(input);
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.add"), button -> addValue())
                .bounds(x + rowWidth - 60, inputY, 60, 20).build());

        addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                    page = Math.max(0, page - 1);
                    rebuildWidgets();
                }).bounds(width / 2 - 130, height - 28, 40, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> done())
                .bounds(width / 2 - 86, height - 28, 172, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> {
                    page = Math.min(pageCount - 1, page + 1);
                    rebuildWidgets();
                }).bounds(width / 2 + 90, height - 28, 40, 20).build());
    }

    private void addValue() {
        String value = input.getValue().trim();
        if (!value.contains(".") || value.startsWith(".") || value.endsWith(".")) {
            error = Component.translatable("scalingguis.config.add.error").withStyle(ChatFormatting.RED);
            return;
        }
        values().add(value);
        error = Component.empty();
        page = Integer.MAX_VALUE;
        ConfigManager.save();
        rebuildWidgets();
    }

    private Set<String> values() {
        return kind == Kind.BLACKLIST ? config.blacklistGuiClassNames : config.dynamicGuiScales;
    }

    private static String shorten(String value, int maximum) {
        return value.length() <= maximum ? value : value.substring(0, maximum - 1) + "…";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 13, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("scalingguis.config.page", page + 1, pageCount),
                width / 2, 25, 0xA0A0A0);
        if (!error.getString().isEmpty()) graphics.drawCenteredString(font, error, width / 2, height - 88, 0xFF5555);
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
