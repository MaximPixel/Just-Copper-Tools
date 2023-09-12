package net.maximpixel.jct.forge;

import dev.architectury.platform.forge.EventBuses;
import net.maximpixel.jct.JustCopperTools;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JustCopperTools.MODID)
public class JustCopperToolsForge extends JustCopperTools {

    public JustCopperToolsForge() {
        EventBuses.registerModEventBus(MODID, FMLJavaModLoadingContext.get().getModEventBus());
        init();
    }
}