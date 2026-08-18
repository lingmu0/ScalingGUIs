package spazley.scalingguis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spazley.scalingguis.client.gui.ScalingConfigScreen;

@Mixin(OptionsScreen.class)
abstract class OptionsScreenMixin {
    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout;createRowHelper(I)Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;"
            )
    )
    private GridLayout.RowHelper scalingguis$rememberRowHelper(GridLayout gridLayout, int columns) {
        scalingguis$gridLayout = gridLayout;
        scalingguis$rowHelper = gridLayout.createRowHelper(columns);
        return scalingguis$rowHelper;
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"
            )
    )
    private void scalingguis$appendConfigButton(GridLayout gridLayout) {
        if (gridLayout == scalingguis$gridLayout && scalingguis$rowHelper != null) {
            scalingguis$rowHelper.addChild(scalingguis$createConfigButton());
        }
        gridLayout.arrangeElements();
    }

    @Unique
    private GridLayout scalingguis$gridLayout;

    @Unique
    private GridLayout.RowHelper scalingguis$rowHelper;

    @Unique
    private LayoutElement scalingguis$createConfigButton() {
        Screen optionsScreen = (Screen) (Object) this;
        return Button.builder(
                        Component.translatable("scalingguis.videosettings.button"),
                        button -> Minecraft.getInstance().setScreen(new ScalingConfigScreen(optionsScreen)))
                .width(150)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("scalingguis.videosettings.button.tooltip")))
                .build();
    }
}
