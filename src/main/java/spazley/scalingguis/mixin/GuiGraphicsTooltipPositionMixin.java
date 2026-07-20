package spazley.scalingguis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spazley.scalingguis.client.ScaleController;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Scales vanilla tooltip fallbacks after replacement mods have declined to
 * handle them. Position inputs are converted at their actual call sites so a
 * cancellable HEAD injection cannot cause a duplicate conversion.
 */
@Mixin(value = GuiGraphics.class, priority = 900)
abstract class GuiGraphicsTooltipPositionMixin {
    @Shadow
    @Final
    private PoseStack pose;

    @Unique
    private final Deque<Boolean> scalingguis$scaledVanillaTooltips = new ArrayDeque<>();

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void scalingguis$trackVanillaTooltip(CallbackInfo callback) {
        scalingguis$scaledVanillaTooltips.push(false);
    }

    @ModifyArg(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onRenderTooltipPre(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/gui/GuiGraphics;IIIILjava/util/List;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)Lnet/minecraftforge/client/event/RenderTooltipEvent$Pre;",
                    remap = false
            ),
            index = 2
    )
    private int scalingguis$scaleVanillaTooltipMouseX(int value) {
        return scalingguis$scaleVanillaTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onRenderTooltipPre(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/gui/GuiGraphics;IIIILjava/util/List;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)Lnet/minecraftforge/client/event/RenderTooltipEvent$Pre;",
                    remap = false
            ),
            index = 3
    )
    private int scalingguis$scaleVanillaTooltipMouseY(int value) {
        return scalingguis$scaleVanillaTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onRenderTooltipPre(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/gui/GuiGraphics;IIIILjava/util/List;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)Lnet/minecraftforge/client/event/RenderTooltipEvent$Pre;",
                    remap = false
            ),
            index = 4
    )
    private int scalingguis$scaleVanillaTooltipScreenWidth(int value) {
        return scalingguis$scaleVanillaTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onRenderTooltipPre(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/gui/GuiGraphics;IIIILjava/util/List;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)Lnet/minecraftforge/client/event/RenderTooltipEvent$Pre;",
                    remap = false
            ),
            index = 5
    )
    private int scalingguis$scaleVanillaTooltipScreenHeight(int value) {
        return scalingguis$scaleVanillaTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"
            ),
            index = 0
    )
    private int scalingguis$scaleVanillaTooltipPositionerWidth(int value) {
        return scalingguis$scaleVanillaTooltipCoordinate(value);
    }

    @ModifyArg(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"
            ),
            index = 1
    )
    private int scalingguis$scaleVanillaTooltipPositionerHeight(int value) {
        return scalingguis$scaleVanillaTooltipCoordinate(value);
    }

    @Unique
    private static int scalingguis$scaleVanillaTooltipCoordinate(int value) {
        if (!ScaleController.shouldScaleVanillaTooltip()) return value;
        return Math.round(value / ScaleController.tooltipScaleRatio());
    }

    @Inject(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void scalingguis$beginVanillaTooltipScale(CallbackInfo callback) {
        boolean scaled = ScaleController.shouldScaleLateVanillaTooltipFallback();
        float ratio = ScaleController.tooltipScaleRatio();
        scaled &= Math.abs(ratio - 1.0F) > 0.0001F;

        if (!scalingguis$scaledVanillaTooltips.isEmpty()) {
            scalingguis$scaledVanillaTooltips.pop();
        }
        scalingguis$scaledVanillaTooltips.push(scaled);
        if (scaled) {
            pose.pushPose();
            pose.scale(ratio, ratio, 1.0F);
        }
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void scalingguis$endVanillaTooltipScale(CallbackInfo callback) {
        if (!scalingguis$scaledVanillaTooltips.isEmpty()
                && scalingguis$scaledVanillaTooltips.pop()) {
            pose.popPose();
        }
    }
}
