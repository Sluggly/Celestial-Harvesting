package io.github.sluggly.celestialharvesting.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MainScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/harvester_gui.png");

    private final Harvester harvester;

    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    private Button repairButton;

    public MainScreen(Harvester harvester) {
        super(Component.literal("Harvester Screen"));
        this.harvester = harvester;
    }

    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int buttonY = this.topPos + this.imageHeight - 28;
        int buttonX = this.leftPos + 8;

        assert minecraft != null;
        addRenderableWidget(Button.builder(Component.literal("Start Mission"), (button) -> this.minecraft.setScreen(new MissionSelectionScreen(this.harvester))).bounds(buttonX, buttonY - 60, 80, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Inventory"), (button) -> {
            CompoundTag data = new CompoundTag();
            data.putLong(NBTKeys.BLOCK_POS, this.harvester.getBlockPos().asLong());
            PacketHandler.sendToServer(new CtoSPacket(NBTKeys.ACTION_OPEN_HARVESTER_INVENTORY, data));
        }).bounds(buttonX, buttonY - 40, 80, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Upgrades"),
                        (button) -> this.minecraft.setScreen(new UpgradeScreen(this.harvester, this)))
                .bounds(buttonX, buttonY - 20, 80, 20).build());

        this.repairButton = addRenderableWidget(Button.builder(Component.literal("Repair"),
                        (button) -> {
                            CompoundTag data = new CompoundTag();
                            data.putLong(NBTKeys.BLOCK_POS, this.harvester.getBlockPos().asLong());
                            PacketHandler.sendToServer(new CtoSPacket(NBTKeys.ACTION_REPAIR_HARVESTER, data));
                        })
                .bounds(buttonX + 90, buttonY, 50, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        renderGui(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltips(pGuiGraphics, pMouseX, pMouseY);
    }

    private void renderGui(GuiGraphics pGuiGraphics) {
        int currentHealth = this.harvester.getHarvesterData().getCurrentHealth();
        int maxHealth = this.harvester.getHarvesterData().getMaxHealth();
        this.repairButton.active = currentHealth < maxHealth;

        RenderSystem.setShaderTexture(0, TEXTURE);
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        renderEnergyBar(pGuiGraphics);

        Component title = Component.literal("Harvester Tier " + this.harvester.getHarvesterData().getTier());
        pGuiGraphics.drawCenteredString(this.font, title, this.width / 2, this.topPos + 8, 0xFFFFFF);

        int percentage = (int) (((float) currentHealth / maxHealth) * 100);
        Component hullText = Component.literal("Hull: " + percentage + "%");
        pGuiGraphics.drawString(this.font, hullText, this.leftPos + 145, this.topPos + this.imageHeight - 22, 0xFFFFFF, true);

        int occupiedSlots = getOccupiedSlots();
        int totalSlots = this.harvester.getItemHandler().getSlots();
        int availableSlots = totalSlots - occupiedSlots;

        final int color;
        if (availableSlots <= 5) { color = 0xFF5555; }
        else if (occupiedSlots <= 20) { color = 0x55FF55; }
        else { color = 0xFFFFFF; }

        Component inventoryText = Component.literal(occupiedSlots + " / " + totalSlots);
        int buttonY = this.topPos + this.imageHeight - 28;
        int buttonX = this.leftPos + 8;

        int textX = buttonX + 80 + 5;
        int textY = buttonY - 40 + 6;

        pGuiGraphics.drawString(this.font, inventoryText, textX, textY, color, true);
    }

    private void renderEnergyBar(GuiGraphics pGuiGraphics) {
        int energy = this.harvester.getEnergyStored();
        int maxEnergy = this.harvester.getMaxEnergyStored();

        if (maxEnergy > 0) {
            int barWidth = 13;
            int barHeight = 60;
            int scaledEnergy = (int)(((float)energy / maxEnergy) * barHeight);

            int energyBarX = this.leftPos + this.imageWidth - 28;
            int energyBarY = this.topPos + 20;
            pGuiGraphics.fill(energyBarX, energyBarY, energyBarX + barWidth, energyBarY + barHeight, 0xFF303030);
            pGuiGraphics.fill(energyBarX, energyBarY + (barHeight - scaledEnergy), energyBarX + barWidth, energyBarY + barHeight, 0xFFFF0000);
        }
    }

    private void renderTooltips(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int energyBarX = this.leftPos + this.imageWidth - 28;
        int energyBarY = this.topPos + 20;
        if (pMouseX >= energyBarX && pMouseX < energyBarX + 13 && pMouseY >= energyBarY && pMouseY < energyBarY + 60) {
            Component text = Component.literal(this.harvester.getEnergyStored() + " / " + this.harvester.getMaxEnergyStored() + " FE");
            pGuiGraphics.renderTooltip(this.font, text, pMouseX, pMouseY);
        }
    }

    private int getOccupiedSlots() {
        int occupiedSlots = 0;
        for (int i = 0; i < this.harvester.getItemHandler().getSlots(); i++) {
            if (!this.harvester.getItemHandler().getStackInSlot(i).isEmpty()) { occupiedSlots++; }
        }
        return occupiedSlots;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}