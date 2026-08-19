package spazley.scalingguis.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spazley.scalingguis.client.ScaleController;

@Mixin(Minecraft.class)
abstract class MinecraftResizeDisplayMixin {
    @Inject(method = "resizeDisplay", at = @At("TAIL"))
    private void scalingguis$restoreConfiguredScale(CallbackInfo callbackInfo) {
        ScaleController.refreshAfterVanillaResize();
    }
}
