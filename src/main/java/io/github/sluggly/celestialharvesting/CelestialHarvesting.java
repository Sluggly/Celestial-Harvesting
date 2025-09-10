package io.github.sluggly.celestialharvesting;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CelestialHarvesting.MOD_ID)
public class CelestialHarvesting {
    public static final String MOD_ID = "celestialharvesting";
    public static final String MOD_VERSION = "0.1.0";
    public static final int NBTVersion = 1;
    public CelestialHarvesting() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

    }
}