package io.github.sluggly.celestialharvesting.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class UpgradeScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/background_space.png");

    private final Harvester harvester;
    private final Screen lastScreen; // To go back to the MainScreen

    public UpgradeScreen(Harvester harvester, Screen lastScreen) {
        super(Component.literal("Upgrades"));
        this.harvester = harvester;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        // Back Button
        addRenderableWidget(Button.builder(Component.literal("< Back"),
                        (button) -> this.minecraft.setScreen(this.lastScreen))
                .bounds(5, 5, 50, 20).build());

        // We will add the actual upgrade buttons here later
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int textureSize = 256;
        int screenWidth = this.width;
        int screenHeight = this.height;

        for (int x = 0; x < screenWidth; x += textureSize) {
            for (int y = 0; y < screenHeight; y += textureSize) {
                pGuiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, Math.min(textureSize, screenWidth - x), Math.min(textureSize, screenHeight - y));
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}