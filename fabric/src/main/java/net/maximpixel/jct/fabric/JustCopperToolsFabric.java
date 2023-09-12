package net.maximpixel.jct.fabric;

import net.maximpixel.jct.JustCopperTools;
import net.fabricmc.api.ModInitializer;

public class JustCopperToolsFabric extends JustCopperTools implements ModInitializer {
    @Override
    public void onInitialize() {
        init();
    }
}