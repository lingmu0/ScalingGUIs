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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class ClassScaleScreen extends Screen {
    private final Screen parent;
    private final boolean group;
    private final CustomScales config = ConfigManager.get();
    private final List<RowLabel> rowLabels = new ArrayList<>();
    private EditBox classNameBox;
    private EditBox displayNameBox;
    private Component error = Component.empty();
    private int page;
    private int pageCount = 1;

    ClassScaleScreen(Screen parent, boolean group) {
        super(Component.translatable(group ? "scalingguis.config.group.title" :
                "scalingguis.config.individual.title"));
        this.parent = parent;
        this.group = group;
    }

    @Override
    protected void init() {
        rowLabels.clear();
        Map<String, CustomScales.ScaleEntry> entries = entries();
        List<Map.Entry<String, CustomScales.ScaleEntry>> sorted = entries.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int pageSize = Math.max(1, (height - 140) / 42);
        pageCount = Math.max(1, (sorted.size() + pageSize - 1) / pageSize);
        page = Math.max(0, Math.min(page, pageCount - 1));
        int from = page * pageSize;
        int to = Math.min(sorted.size(), from + pageSize);
        int x = width / 2 - Math.min(180, (width - 20) / 2);
        int rowWidth = Math.min(360, width - 20);
        int y = 35;

        for (Map.Entry<String, CustomScales.ScaleEntry> mapEntry : sorted.subList(from, to)) {
            String className = mapEntry.getKey();
            CustomScales.ScaleEntry entry = mapEntry.getValue();
            ScaleSlider slider = new ScaleSlider(x, y, rowWidth - 64,
                    Component.literal(shorten(entry.name, 28)),
                    Component.literal(className), entry.scale, true,
                    value -> entry.scale = value);
            addRenderableWidget(slider);
            addRenderableWidget(Button.builder(Component.literal("×"), button -> {
                        entries.remove(className);
                        ConfigManager.save();
                        rebuildWidgets();
                    }).bounds(x + rowWidth - 60, y, 60, 20).build());
            rowLabels.add(new RowLabel(x + 3, y + 24, shorten(className, 55)));
            y += 42;
        }

        int inputY = height - 76;
        int classWidth = Math.max(100, rowWidth - 170);
        classNameBox = new EditBox(font, x, inputY, classWidth, 20,
                Component.translatable("scalingguis.config.add.classname"));
        classNameBox.setHint(Component.translatable("scalingguis.config.add.classname"));
        classNameBox.setMaxLength(512);
        addRenderableWidget(classNameBox);

        displayNameBox = new EditBox(font, x + classWidth + 4, inputY, 106, 20,
                Component.translatable("scalingguis.config.add.displayname"));
        displayNameBox.setHint(Component.translatable("scalingguis.config.add.displayname"));
        displayNameBox.setMaxLength(128);
        addRenderableWidget(displayNameBox);
        addRenderableWidget(Button.builder(Component.translatable("scalingguis.config.add"), button -> addEntry())
                .bounds(x + classWidth + 114, inputY, rowWidth - classWidth - 114, 20).build());

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

    private void addEntry() {
        String className = classNameBox.getValue().trim();
        if (!looksLikeClassName(className)) {
            error = Component.translatable("scalingguis.config.add.error").withStyle(ChatFormatting.RED);
            return;
        }

        String displayName = displayNameBox.getValue().trim();
        if (displayName.isEmpty()) {
            int separator = Math.max(className.lastIndexOf('.'), className.lastIndexOf('$'));
            displayName = className.substring(separator + 1);
        }
        entries().put(className, new CustomScales.ScaleEntry(displayName, CustomScales.MAIN_GUI_SCALE));
        error = Component.empty();
        page = Integer.MAX_VALUE;
        ConfigManager.save();
        rebuildWidgets();
    }

    private Map<String, CustomScales.ScaleEntry> entries() {
        return group ? config.customGroupGuiScales : config.customIndividualGuiScales;
    }

    private static boolean looksLikeClassName(String value) {
        if (value.isBlank() || value.startsWith(".") || value.endsWith(".")) return false;
        for (String part : value.split("\\.")) {
            if (part.isBlank() || !Character.isJavaIdentifierStart(part.charAt(0))) return false;
            for (int i = 1; i < part.length(); i++) {
                char character = part.charAt(i);
                if (!Character.isJavaIdentifierPart(character) && character != '$') return false;
            }
        }
        return true;
    }

    private static String shorten(String value, int maximum) {
        if (value == null) return "";
        return value.length() <= maximum ? value : value.substring(0, Math.max(1, maximum - 1)) + "…";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 13, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("scalingguis.config.page", page + 1, pageCount),
                width / 2, 24, 0xA0A0A0);
        for (RowLabel row : rowLabels) graphics.drawString(font, row.text(), row.x(), row.y(), 0x808080, false);
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

    private record RowLabel(int x, int y, String text) {
    }
}
