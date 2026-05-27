package com.danzer.p2pdetails;

import net.minecraftforge.common.config.Config;

@Config(modid = P2PDetailsMod.MOD_ID, name = P2PDetailsMod.MOD_ID)
public class P2PDetailsConfig {

    @Config.Comment({
        "Whether to append generic AE2 channel usage lines in TOP for cables/parts.",
        "Disable this to avoid duplicate channel lines with TOP's built-in display."
    })
    public static boolean showGenericChannelInfo = false;

    @Config.Comment({
        "How many channels left before entering the near-capacity warning state.",
        "Set to 0 to disable near-capacity warnings."
    })
    @Config.RangeInt(min = 0, max = 64)
    public static int nearCapacityReserve = 4;

    @Config.Comment("Max output entries shown in TOP EXTENDED mode.")
    @Config.RangeInt(min = 1, max = 32)
    public static int extendedOutputLimit = 4;

    @Config.Comment("Max detail lines per category in /p2pdetails scan output.")
    @Config.RangeInt(min = 1, max = 20)
    public static int scanDetailLines = 6;

    private P2PDetailsConfig() {
    }
}
