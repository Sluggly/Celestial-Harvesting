package io.github.sluggly.celestialharvesting.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class MainScreen extends AbstractContainerScreen<HarvesterMenu> {
    // Path to your custom background texture
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/harvester_gui.png");

    public MainScreen(HarvesterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        // Set the size of your GUI window
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        assert minecraft != null;
        // Add the buttons. The onPress action sends a packet to the server via clickMenuButton.
        addRenderableWidget(Button.builder(Component.literal("Start Mission"), (button) -> this.minecraft.setScreen(new MissionSelectionScreen(this.menu.blockEntity))).bounds(x + 8, y + 20, 80, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Upgrades"),
                        (button) -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1))
                .bounds(x + 8, y + 42, 80, 20).build());

        // This button is for demonstration. It calls button ID 2 in your menu.
        addRenderableWidget(Button.builder(Component.literal("Repair"),
                        (button) -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2))
                .bounds(x + 90, y + 20, 50, 20).build());
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Draw the background texture
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render the health bar
        renderHealthBar(pGuiGraphics, x, y);
    }

    private void renderHealthBar(GuiGraphics pGuiGraphics, int x, int y) {
        int health = this.menu.getHealth();
        int maxHealth = this.menu.getMaxHealth();

        if (maxHealth > 0) {
            int barWidth = 13; // The width of the health bar in pixels
            int scaledHealth = (int)(((float)health / maxHealth) * barWidth);

            // Draw the health bar itself (e.g., at UV coordinates 176, 0 with a width of 13 and height of 40)
            // This assumes you have a health bar graphic on your texture sheet.
            // pGuiGraphics.blit(TEXTURE, x + 152, y + 15, 176, 0, scaledHealth, 40);

            // For now, let's just draw a colored rectangle as a placeholder
            int healthBarX = x + 152;
            int healthBarY = y + 15;
            int healthBarHeight = 40;
            pGuiGraphics.fill(healthBarX, healthBarY + (healthBarHeight - scaledHealth), healthBarX + barWidth, healthBarY + healthBarHeight, 0xFF00FF00); // Green
        }
    }


    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // First, draw the dark background overlay
        this.renderBackground(pGuiGraphics);
        // Then, draw the GUI elements (background, health bar, etc.)
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        // Finally, draw tooltips (like player inventory item names) on top of everything.
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        // Draw the title of your GUI
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        // Draw the player inventory title
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // Draw the Hull Integrity text
        pGuiGraphics.drawString(this.font, Component.literal("Hull Integrity"), this.titleLabelX + 85, this.titleLabelY + 10, 4210752, false);
    }
}