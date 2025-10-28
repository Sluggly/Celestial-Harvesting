package io.github.sluggly.celestialharvesting.client.screen;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class HarvesterInventoryScreen extends AbstractContainerScreen<HarvesterInventoryMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/harvester_inventory_gui.png");

    public HarvesterInventoryScreen(HarvesterInventoryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;

        int harvesterRows = pMenu.getHarvesterRows();
        this.imageHeight = (17 + harvesterRows * 18) + 14 + 94;
        this.inventoryLabelY = this.imageHeight - 104;
    }

    private static final int TEX_W = 176;
    private static final int TEX_H = 222;
    private static final int PLAYER_INV_H = 96;

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int rows = this.menu.getHarvesterRows();
        int harvesterSectionHeight = 17 + rows * 18;

        g.blit(TEXTURE, x, y, 0, 0, this.imageWidth, harvesterSectionHeight, TEX_W, TEX_H);

        int vPlayerStart = TEX_H - PLAYER_INV_H;
        g.blit(TEXTURE, x, y + harvesterSectionHeight, 0, vPlayerStart, this.imageWidth, PLAYER_INV_H, TEX_W, TEX_H);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}