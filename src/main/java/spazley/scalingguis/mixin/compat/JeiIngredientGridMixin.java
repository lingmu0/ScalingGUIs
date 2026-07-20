package spazley.scalingguis.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets JEI use every row and column that fits in its available display area.
 *
 * <p>JEI normally caps both the ingredient and bookmark grids at the configured
 * maximum row and column counts (16 and 9 by default). When Scaling GUIs makes
 * a screen smaller, the increased logical screen size can fit more entries, but
 * those caps leave unused strips around JEI's overlays.</p>
 */
@Pseudo
@Mixin(targets = "mezz.jei.gui.overlay.IngredientGrid", remap = false)
public abstract class JeiIngredientGridMixin {
    @Redirect(
            method = "calculateSize",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/common/config/IIngredientGridConfig;getMaxColumns()I",
                    remap = false
            ),
            require = 0
    )
    private static int scalingguis$useAvailableWidth(@Coerce Object config) {
        return Integer.MAX_VALUE;
    }

    @Redirect(
            method = "calculateSize",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/common/config/IIngredientGridConfig;getMaxRows()I",
                    remap = false
            ),
            require = 0
    )
    private static int scalingguis$useAvailableHeight(@Coerce Object config) {
        return Integer.MAX_VALUE;
    }
}
