package com.danzer.p2pdetails;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;

@Mod(
    modid = P2PDetailsMod.MOD_ID,
    name = P2PDetailsMod.MOD_NAME,
    version = P2PDetailsMod.VERSION,
    acceptableRemoteVersions = "*",
    dependencies = "required-after:appliedenergistics2;after:theoneprobe"
)
public class P2PDetailsMod {

    public static final String MOD_ID = "p2pdetails";
    public static final String MOD_NAME = "P2P Details";
    public static final String VERSION = "1.0.1";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (Loader.isModLoaded("theoneprobe")) {
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", TopCompat.class.getName());
        }
    }
}
