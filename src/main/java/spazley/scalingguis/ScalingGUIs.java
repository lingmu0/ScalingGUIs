package spazley.scalingguis;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import spazley.scalingguis.client.ClientKeyMappings;
import spazley.scalingguis.client.ScaleController;
import spazley.scalingguis.client.gui.ScalingConfigScreen;

@Mod(value = ScalingGUIs.MODID, dist = Dist.CLIENT)
public final class ScalingGUIs {
    public static final String MODID = "scalingguis";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ScalingGUIs(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(ClientKeyMappings::register);
        NeoForge.EVENT_BUS.register(new ScaleController());
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (ignoredContainer, parent) -> new ScalingConfigScreen(parent));
    }
}
