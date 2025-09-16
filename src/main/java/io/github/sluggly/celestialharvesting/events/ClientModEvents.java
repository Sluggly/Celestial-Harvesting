package io.github.sluggly.celestialharvesting.events;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.model.HarvesterModel;
import io.github.sluggly.celestialharvesting.client.renderer.HarvesterRenderer;
import io.github.sluggly.celestialharvesting.client.screen.HarvesterInventoryScreen;
import io.github.sluggly.celestialharvesting.client.screen.widget.ItemListTooltipComponent;
import io.github.sluggly.celestialharvesting.client.screen.widget.ItemListTooltipData;
import io.github.sluggly.celestialharvesting.init.BlockEntityInit;
import io.github.sluggly.celestialharvesting.init.MenuInit;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityInit.HARVESTER.get(), HarvesterRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HarvesterModel.LAYER_LOCATION, HarvesterModel::createBodyLayer);
    }
}
