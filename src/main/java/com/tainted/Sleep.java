package com.tainted;

import com.tainted.common.capability.Status;
import com.tainted.common.config.Config;
import com.tainted.common.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;


@Mod("sleep")
public class Sleep {
    public static final String MODID = "sleep";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Marker MARKER = MarkerManager.getMarker(MODID);

    public Sleep() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Sleep::setup);

        MinecraftForge.EVENT_BUS.addListener(this::registerCapabilities);

        bus.addListener(Sleep::commonSetup);

        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    public static void setup(final FMLCommonSetupEvent event) {
    }

    public static void commonSetup(final @NotNull FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::registerMessages);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(Status.class);
    }

}
