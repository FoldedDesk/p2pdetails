package com.danzer.p2pdetails;

import java.util.function.Function;

import mcjty.theoneprobe.api.ITheOneProbe;

public class TopCompat implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(new TopProvider());
        return null;
    }
}
