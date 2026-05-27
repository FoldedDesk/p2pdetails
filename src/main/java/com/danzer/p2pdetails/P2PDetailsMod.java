package com.danzer.p2pdetails;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(
    modid = P2PDetailsMod.MOD_ID,
    name = P2PDetailsMod.MOD_NAME,
    version = P2PDetailsMod.VERSION,
    acceptableRemoteVersions = "*",
    dependencies = "required-after:appliedenergistics2;after:theoneprobe"
)
@Mod.EventBusSubscriber(modid = P2PDetailsMod.MOD_ID)
public class P2PDetailsMod {

    public static final String MOD_ID = "p2pdetails";
    public static final String MOD_NAME = "P2P Details";
    public static final String VERSION = "1.0.6";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (Loader.isModLoaded("theoneprobe")) {
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", TopCompat.class.getName());
        }
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ScanCommand());
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (MOD_ID.equals(event.getModID())) {
            ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
        }
    }
}
