package spazley.scalingguis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
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
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;ILnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;"
            )
    )
    private LayoutElement scalingguis$insertConfigButton(GridLayout.RowHelper rowHelper,
                                                          LayoutElement child,
                                                          int columnSpan,
                                                          LayoutSettings settings) {
        rowHelper.addChild(scalingguis$createConfigButton());
        return rowHelper.addChild(child, columnSpan, settings);
    }

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
