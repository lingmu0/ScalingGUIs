package spazley.scalingguis.mixin.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spazley.scalingguis.client.ScaleController;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Obscure Tooltips replaces GuiGraphics#renderTooltipInternal at HEAD, so the
 * vanilla coordinate redirects never run. Scale inside its own renderer and
 * convert its positioner inputs exactly once.
 */
@Pseudo
@Mixin(targets = "dev.obscuria.tooltips.client.TooltipRenderer", remap = false)
abstract class ObscureTooltipsTooltipRendererMixin {
    @Unique
    private static final ThreadLocal<Deque<Boolean>> scalingguis$scaledTooltips =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private static void scalingguis$beginObscureTooltipScale(
            GuiGraphics graphics, Font font, List<ClientTooltipComponent> components,
            int mouseX, int mouseY, ClientTooltipPositioner positioner,
            CallbackInfoReturnable<Boolean> callback) {
        float ratio = ScaleController.tooltipScaleRatio();
        boolean scaled = Math.abs(ratio - 1.0F) > 0.0001F;
        scalingguis$scaledTooltips.get().push(scaled);
        if (scaled) {
            graphics.pose().pushPose();
            graphics.pose().scale(ratio, ratio, 1.0F);
        }
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private static void scalingguis$endObscureTooltipScale(
            GuiGraphics graphics, Font font, List<ClientTooltipComponent> components,
            int mouseX, int mouseY, ClientTooltipPositioner positioner,
            CallbackInfoReturnable<Boolean> callback) {
        Deque<Boolean> stack = scalingguis$scaledTooltips.get();
        if (!stack.isEmpty() && stack.pop()) graphics.pose().popPose();
        if (stack.isEmpty()) scalingguis$scaledTooltips.remove();
        ScaleController.recordObscureTooltipResult(Boolean.TRUE.equals(callback.getReturnValue()));
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;",
                    remap = true
            ),
            index = 0,
            require = 0,
            remap = false
    )
    private static int scalingguis$scaleObscureTooltipScreenWidth(int value) {
        return scalingguis$scaleObscureTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;",
                    remap = true
            ),
            index = 1,
            require = 0,
            remap = false
    )
    private static int scalingguis$scaleObscureTooltipScreenHeight(int value) {
        return scalingguis$scaleObscureTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;",
                    remap = true
            ),
            index = 2,
            require = 0,
            remap = false
    )
    private static int scalingguis$scaleObscureTooltipMouseX(int value) {
        return scalingguis$scaleObscureTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;",
                    remap = true
            ),
            index = 3,
            require = 0,
            remap = false
    )
    private static int scalingguis$scaleObscureTooltipMouseY(int value) {
        return scalingguis$scaleObscureTooltipCoordinate(value);
    }

    @Unique
    private static int scalingguis$scaleObscureTooltipCoordinate(int value) {
        float ratio = ScaleController.tooltipScaleRatio();
        return Math.abs(ratio - 1.0F) <= 0.0001F ? value : Math.round(value / ratio);
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/obscuria/tooltips/client/tooltip/TooltipScroll;update(Ldev/obscuria/tooltips/client/TooltipState;II)V",
                    remap = false
            ),
            index = 2,
            require = 0
    )
    private static int scalingguis$scaleObscureTooltipScrollBounds(int screenHeight) {
        return Math.round(screenHeight / ScaleController.tooltipScaleRatio());
    }
}
