package com.danzer.p2pdetails;

import java.lang.reflect.Method;

import appeng.parts.p2p.PartP2PTunnel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public static final String VERSION = "1.0.8";
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    private static final Method SET_FREQUENCY_METHOD = findSetFrequencyMethod();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NETWORK.registerMessage(PacketSetNextFrequency.Handler.class, PacketSetNextFrequency.class, 0, Side.SERVER);
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

    public static boolean setTunnelFrequency(PartP2PTunnel<?> tunnel, short frequency) {
        if (SET_FREQUENCY_METHOD == null) {
            return false;
        }
        try {
            SET_FREQUENCY_METHOD.invoke(tunnel, frequency);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static Method findSetFrequencyMethod() {
        try {
            Method method = PartP2PTunnel.class.getDeclaredMethod("setFrequency", short.class);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void openP2PConfigGui() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiP2PConfigTool());
    }
}
