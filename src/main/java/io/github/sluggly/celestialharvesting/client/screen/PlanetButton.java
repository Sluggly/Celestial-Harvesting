package io.github.sluggly.celestialharvesting.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sluggly.celestialharvesting.mission.Mission;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PlanetButton extends Button {
    private final Mission mission;
    private final ResourceLocation icon;

    public PlanetButton(int pX, int pY, int pWidth, int pHeight, Mission mission, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, DEFAULT_NARRATION);
        this.mission = mission;
        this.icon = mission.getIcon();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, this.icon);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (this.isHoveredOrFocused()) {
            pGuiGraphics.renderOutline(this.getX() - 1, this.getY() - 1, this.width + 2, this.height + 2, 0xFFFFFF00); // Yellow
        }
        pGuiGraphics.blit(this.icon, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
    }

    public Mission getMission() { return this.mission; }
}