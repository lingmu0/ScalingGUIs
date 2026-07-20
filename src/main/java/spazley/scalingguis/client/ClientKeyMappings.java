package spazley.scalingguis.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyMappings {
    private static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.scalingguis.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.scalingguis"
    );

    private ClientKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    public static boolean consumeOpenConfig() {
        boolean pressed = OPEN_CONFIG.consumeClick();
        while (OPEN_CONFIG.consumeClick()) {
            // Collapse repeated key events into a single screen opening.
        }
        return pressed;
    }
}
