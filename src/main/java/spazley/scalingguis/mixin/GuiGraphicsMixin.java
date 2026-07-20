package spazley.scalingguis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spazley.scalingguis.client.ScaleController;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Mixin(value = GuiGraphics.class, priority = 1100)
abstract class GuiGraphicsMixin {
    @Shadow
    @Final
    private PoseStack pose;

    @Unique
    private final Deque<Boolean> scalingguis$scaledTooltips = new ArrayDeque<>();

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void scalingguis$beginTooltipScale(Font font, List<ClientTooltipComponent> components,
                                                int mouseX, int mouseY,
                                                ClientTooltipPositioner positioner, CallbackInfo callback) {
        ScaleController.beginTooltipRender(components);
        float ratio = ScaleController.tooltipScaleRatio();
        boolean scaled = ScaleController.shouldScaleVanillaTooltip()
                && Math.abs(ratio - 1.0F) > 0.0001F;
        scalingguis$scaledTooltips.push(scaled);
        if (scaled) {
            pose.pushPose();
            pose.scale(ratio, ratio, 1.0F);
        }
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void scalingguis$endTooltipScale(Font font, List<ClientTooltipComponent> components,
                                              int mouseX, int mouseY,
                                              ClientTooltipPositioner positioner, CallbackInfo callback) {
        if (!scalingguis$scaledTooltips.isEmpty() && scalingguis$scaledTooltips.pop()) pose.popPose();
        ScaleController.endTooltipRender();
    }
}
