package spazley.scalingguis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spazley.scalingguis.client.gui.ScalingConfigScreen;

import java.util.function.Consumer;

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
        scalingguis$rowHelper = gridLayout.createRowHelper(columns);
        return scalingguis$rowHelper;
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;visitWidgets(Ljava/util/function/Consumer;)V"
            )
    )
    private void scalingguis$appendConfigButton(HeaderAndFooterLayout layout,
                                                Consumer<AbstractWidget> consumer) {
        if (scalingguis$rowHelper != null) {
            scalingguis$rowHelper.addChild(scalingguis$createConfigButton());
        }
        layout.visitWidgets(consumer);
    }

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
