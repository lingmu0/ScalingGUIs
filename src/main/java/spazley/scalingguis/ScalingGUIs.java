package spazley.scalingguis;

import com.mojang.logging.LogUtils;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import spazley.scalingguis.client.ClientKeyMappings;
import spazley.scalingguis.client.ScaleController;
import spazley.scalingguis.client.gui.ScalingConfigScreen;

@Mod(ScalingGUIs.MODID)
public final class ScalingGUIs {
    public static final String MODID = "scalingguis";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ScalingGUIs() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ClientKeyMappings::register);
        MinecraftForge.EVENT_BUS.register(new ScaleController());
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new ScalingConfigScreen(parent)));
    }
}
