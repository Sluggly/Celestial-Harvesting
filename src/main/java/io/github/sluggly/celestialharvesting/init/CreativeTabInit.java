package io.github.sluggly.celestialharvesting.init;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;

@Mod.EventBusSubscriber(modid = CelestialHarvesting.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CelestialHarvesting.MOD_ID);

    static {
        TABS.register("monster_league_tab",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.celestial_harvesting_tab"))
                        .icon(() -> new ItemStack(BlockInit.HARVESTER.get()))
                        .displayItems((displayParams, output) -> {
                            output.accept(() -> new ItemStack(BlockInit.HARVESTER.get()).getItem());
                        })
                        .build()
        );
    }
}

