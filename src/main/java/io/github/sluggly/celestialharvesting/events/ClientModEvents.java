package io.github.sluggly.celestialharvesting.events;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.screen.HarvesterInventoryScreen;
import io.github.sluggly.celestialharvesting.client.screen.widget.ItemListTooltipComponent;
import io.github.sluggly.celestialharvesting.client.screen.widget.ItemListTooltipData;
import io.github.sluggly.celestialharvesting.init.MenuInit;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CelestialHarvesting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(MenuInit.HARVESTER_INVENTORY_MENU.get(), HarvesterInventoryScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ItemListTooltipData.class, ItemListTooltipComponent::new);
    }
}
