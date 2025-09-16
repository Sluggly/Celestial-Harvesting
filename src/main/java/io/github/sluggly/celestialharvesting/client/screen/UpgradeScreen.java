package io.github.sluggly.celestialharvesting.client.screen;

import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.data.UpgradeData;
import io.github.sluggly.celestialharvesting.client.screen.widget.UpgradeButton;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class UpgradeScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/harvester_gui.png");
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/widgets.png");

    private final Harvester harvester;
    private final Screen lastScreen;
    private UpgradeData upgradeData;

    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    public UpgradeScreen(Harvester harvester, Screen lastScreen) {
        super(Component.literal("Upgrades"));
        this.harvester = harvester;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.upgradeData = new UpgradeData(this.harvester);

        // Back Button
        assert minecraft != null;
        addRenderableWidget(Button.builder(Component.literal("< Back"),
                        (button) -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.leftPos + 5, this.topPos + 5, 50, 20).build());

        // Create buttons for each upgrade
        for (UpgradeData.UpgradeDisplayInfo info : this.upgradeData.upgradeInfoMap.values()) {
            int x = this.leftPos + 15 + (info.def.column() * 24);
            int y = this.topPos + 35 + (info.def.row() * 24);

            UpgradeButton button = new UpgradeButton(x, y, 20, 20, info.id, (btn) -> {
                // Send unlock request to server
                CompoundTag data = new CompoundTag();
                data.putLong(NBTKeys.BLOCK_POS, this.harvester.getBlockPos().asLong());
                data.putString(NBTKeys.UPGRADE_ID, ((UpgradeButton)btn).upgradeId.toString());
                PacketHandler.sendToServer(new CtoSPacket(NBTKeys.ACTION_UNLOCK_UPGRADE, data));
            });

            button.active = info.canUnlock;
            button.setTooltip(Tooltip.create(info.tooltip));
            addRenderableWidget(button);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        renderUpgradeGrid(pGuiGraphics);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick); // Renders buttons and their tooltips

        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.topPos + 10, 0xFFFFFF);
    }

    private void renderUpgradeGrid(GuiGraphics pGuiGraphics) {
        if (this.upgradeData == null) return;

        // Render dependency lines first so they are behind the icons
        for (UpgradeData.UpgradeDisplayInfo info : this.upgradeData.upgradeInfoMap.values()) {
            for (String reqIdStr : info.def.requirements()) {
                ResourceLocation reqId = new ResourceLocation(reqIdStr);
                UpgradeData.UpgradeDisplayInfo reqInfo = this.upgradeData.upgradeInfoMap.get(reqId);
                if (reqInfo != null) {
                    drawDependencyLine(pGuiGraphics, info, reqInfo);
                }
            }
        }

        // Render icons and backgrounds
        for (UpgradeData.UpgradeDisplayInfo info : this.upgradeData.upgradeInfoMap.values()) {
            int x = this.leftPos + 17 + (info.def.column() * 24);
            int y = this.topPos + 37 + (info.def.row() * 24);

            // Draw background if unlocked
            if (info.isUnlocked) {
                pGuiGraphics.blit(WIDGETS_TEXTURE, x - 3, y - 3, 0, 0, 22, 22);
            }

            pGuiGraphics.renderItem(info.icon, x, y);
        }
    }

    private void drawDependencyLine(GuiGraphics pGuiGraphics, UpgradeData.UpgradeDisplayInfo from, UpgradeData.UpgradeDisplayInfo to) {
        int x1 = this.leftPos + 15 + (to.def.column() * 24) + 10;
        int y1 = this.topPos + 35 + (to.def.row() * 24) + 10;
        int x2 = this.leftPos + 15 + (from.def.column() * 24) + 10;
        int y2 = this.topPos + 35 + (from.def.row() * 24) + 10;

        int color = 0xFF808080; // Gray
        if(from.hasUpgradeRequirements) color = 0xFFFFFFFF;

        pGuiGraphics.hLine(Math.min(x1, x2), Math.max(x1, x2), y1, color);
        pGuiGraphics.vLine(x2, Math.min(y1, y2), Math.max(y1, y2), color);
    }
}