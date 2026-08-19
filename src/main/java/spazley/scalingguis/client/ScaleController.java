package spazley.scalingguis.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import spazley.scalingguis.ScalingGUIs;
import spazley.scalingguis.client.gui.ScalingConfigScreen;
import spazley.scalingguis.config.ConfigManager;
import spazley.scalingguis.config.CustomScales;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class ScaleController {
    private static int appliedScale = Integer.MIN_VALUE;
    private static int observedVanillaScale = Integer.MIN_VALUE;
    private static boolean applying;
    private static final ThreadLocal<Deque<ObscureTooltipResult>> OBSCURE_TOOLTIP_RESULTS =
            ThreadLocal.withInitial(ArrayDeque::new);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenOpening(ScreenEvent.Opening event) {
        applyForScreen(event.getNewScreen(), false);
    }

    @SubscribeEvent
    public void onScreenInitialized(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        recordScreen(screen);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        acceptVanillaScaleChange(minecraft);
        if (ClientKeyMappings.consumeOpenConfig() && !(minecraft.screen instanceof ScalingConfigScreen)) {
            minecraft.setScreen(new ScalingConfigScreen(minecraft.screen));
        }
        applyForScreen(minecraft.screen, true);
    }

    public static void refreshCurrentScale() {
        appliedScale = Integer.MIN_VALUE;
        applyForScreen(Minecraft.getInstance().screen, true);
    }

    public static float tooltipScaleRatio() {
        Minecraft minecraft = Minecraft.getInstance();
        int requested = ConfigManager.get().resolve(ConfigManager.get().tooltipScale);
        int tooltipFactor = calculateScaleFactor(minecraft, requested);
        double currentFactor = minecraft.getWindow().getGuiScale();
        return currentFactor <= 0.0 ? 1.0F : (float) tooltipFactor / (float) currentFactor;
    }

    public static boolean isObscureTooltipsLoaded() {
        return ModList.get().isLoaded("obscure_tooltips");
    }

    public static void beginTooltipRender(List<ClientTooltipComponent> components) {
        if (isObscureTooltipsLoaded()) {
            boolean obscureTooltip = components.stream().anyMatch(component ->
                    component.getClass().getName().equals(
                            "dev.obscuria.tooltips.client.component.StackBuffer"));
            OBSCURE_TOOLTIP_RESULTS.get().push(obscureTooltip
                    ? ObscureTooltipResult.OBSCURE
                    : ObscureTooltipResult.VANILLA);
        }
    }

    public static void recordObscureTooltipResult(boolean handled) {
        Deque<ObscureTooltipResult> results = OBSCURE_TOOLTIP_RESULTS.get();
        if (results.isEmpty()) return;
        ObscureTooltipResult initial = results.pop();
        if (handled) {
            results.push(ObscureTooltipResult.HANDLED);
        } else {
            // A tooltip classified as vanilla was already scaled by the
            // GuiGraphics HEAD hook. Only an Obscure-classified tooltip that
            // unexpectedly falls back still needs the late vanilla scale.
            results.push(initial == ObscureTooltipResult.OBSCURE
                    ? ObscureTooltipResult.FALLBACK
                    : ObscureTooltipResult.VANILLA);
        }
    }

    public static boolean shouldScaleVanillaTooltip() {
        if (!isObscureTooltipsLoaded()) return true;
        ObscureTooltipResult result = OBSCURE_TOOLTIP_RESULTS.get().peek();
        return result == ObscureTooltipResult.VANILLA
                || result == ObscureTooltipResult.FALLBACK;
    }

    public static boolean shouldScaleLateVanillaTooltipFallback() {
        return isObscureTooltipsLoaded()
                && OBSCURE_TOOLTIP_RESULTS.get().peek() == ObscureTooltipResult.FALLBACK;
    }

    public static void endTooltipRender() {
        if (!isObscureTooltipsLoaded()) return;
        Deque<ObscureTooltipResult> results = OBSCURE_TOOLTIP_RESULTS.get();
        if (!results.isEmpty()) results.pop();
        if (results.isEmpty()) OBSCURE_TOOLTIP_RESULTS.remove();
    }

    private static void applyForScreen(Screen screen, boolean resizeScreen) {
        if (applying) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null || minecraft.getWindow() == null) return;
        if (observedVanillaScale == Integer.MIN_VALUE) {
            observedVanillaScale = minecraft.options.guiScale().get();
        }

        CustomScales config = ConfigManager.get();
        int desired;
        if (screen == null) {
            desired = config.resolve(config.hudScale);
        } else {
            String className = screen.getClass().getName();
            if (config.blacklistGuiClassNames.contains(className)) return;
            desired = config.dynamicGuiScales.contains(className) && screen instanceof AbstractContainerScreen<?> container
                    ? dynamicScale(minecraft, container)
                    : config.screenScale(screen);
        }

        desired = Math.max(CustomScales.AUTO_SCALE,
                Math.min(CustomScales.MAX_EXPLICIT_SCALE, desired));
        int factor = calculateScaleFactor(minecraft, desired);
        Window window = minecraft.getWindow();
        if (desired == appliedScale && Math.abs(window.getGuiScale() - factor) < 0.001D) return;

        applying = true;
        try {
            window.setGuiScale(factor);
            appliedScale = desired;
            if (resizeScreen && screen != null) {
                screen.resize(minecraft, window.getGuiScaledWidth(), window.getGuiScaledHeight());
            }
        } finally {
            applying = false;
        }
    }

    private static void acceptVanillaScaleChange(Minecraft minecraft) {
        if (applying || appliedScale == Integer.MIN_VALUE
                || minecraft == null || minecraft.options == null) return;

        int vanillaScale = minecraft.options.guiScale().get();
        if (observedVanillaScale == Integer.MIN_VALUE) {
            observedVanillaScale = vanillaScale;
            return;
        }
        if (vanillaScale == observedVanillaScale) return;

        CustomScales config = ConfigManager.get();
        config.resetBaseScalesTo(vanillaScale);
        ConfigManager.save();
        observedVanillaScale = vanillaScale;
        appliedScale = Integer.MIN_VALUE;
    }

    public static void refreshAfterVanillaResize() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null || minecraft.getWindow() == null) return;
        applyForScreen(minecraft.screen, true);
    }

    private static int dynamicScale(Minecraft minecraft, AbstractContainerScreen<?> screen) {
        Window window = minecraft.getWindow();
        boolean forceUnicode = minecraft.options.forceUnicodeFont().get();
        int maximum = window.calculateScale(CustomScales.AUTO_SCALE, forceUnicode);
        for (int candidate = maximum; candidate >= 1; candidate--) {
            int factor = window.calculateScale(candidate, forceUnicode);
            int guiWidth = window.getWidth() / factor;
            int guiHeight = window.getHeight() / factor;
            if (guiWidth > screen.getXSize() && guiHeight > screen.getYSize()) return candidate;
        }
        return 1;
    }

    private static int calculateScaleFactor(Minecraft minecraft, int requested) {
        if (requested == CustomScales.AUTO_SCALE) {
            return minecraft.getWindow().calculateScale(requested,
                    minecraft.options.forceUnicodeFont().get());
        }
        return requested;
    }

    private static void recordScreen(Screen screen) {
        CustomScales config = ConfigManager.get();
        String className = screen.getClass().getName();
        if (config.logGuiClassNames) ScalingGUIs.LOGGER.info("Opened GUI: {}", className);
        if (config.logGuiClassNamesChat && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Opened GUI: " + className), false);
        }
        if (config.persistentLog && config.loggedGuiClassNames.add(className)) ConfigManager.save();
    }

    private enum ObscureTooltipResult {
        VANILLA,
        OBSCURE,
        HANDLED,
        FALLBACK
    }
}
