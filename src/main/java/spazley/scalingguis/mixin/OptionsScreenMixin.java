package spazley.scalingguis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spazley.scalingguis.client.gui.ScalingConfigScreen;

@Mixin(OptionsScreen.class)
abstract class OptionsScreenMixin {
    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
                    ordinal = 11
            )
    )
    private LayoutElement scalingguis$appendConfigButton(GridLayout.RowHelper rowHelper,
                                                          LayoutElement finalVanillaButton) {
        Screen optionsScreen = (Screen) (Object) this;
        LayoutElement result = rowHelper.addChild(finalVanillaButton);
        rowHelper.addChild(Button.builder(
                        Component.translatable("scalingguis.videosettings.button"),
                        button -> Minecraft.getInstance().setScreen(new ScalingConfigScreen(optionsScreen)))
                .width(150)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("scalingguis.videosettings.button.tooltip")))
                .build());
        return result;
    }
}
