package io.github.sluggly.celestialharvesting.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.screen.widget.ItemListTooltipData;
import io.github.sluggly.celestialharvesting.client.screen.widget.PlanetButton;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.harvester.HarvesterData;
import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.mission.MissionManager;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MissionSelectionScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/gui/space_background.png");

    private final Harvester harvester;
    private final HarvesterData harvesterData;

    private static final int PLANET_TEXTURE_WIDTH = 64;
    private static final int PLANET_TEXTURE_HEIGHT = 64;
    private static final int SAFE_ZONE_PADDING = 10;
    private List<MissionDisplayInfo> missionDisplayInfos;
    private record MissionDisplayInfo(Mission mission, int x, int y) {}

    public MissionSelectionScreen(Harvester harvester) {
        super(Component.literal("Mission Selection"));
        this.harvester = harvester;
        this.harvesterData = harvester.getHarvesterData();
    }

    @Override
    protected void init() {
        super.init();
        this.calculateMissionPositions();
        this.clearWidgets();

        if (this.missionDisplayInfos != null) {
            for (MissionDisplayInfo info : this.missionDisplayInfos) {
                addRenderableWidget(new PlanetButton(info.x(), info.y(), PLANET_TEXTURE_WIDTH, PLANET_TEXTURE_HEIGHT, info.mission(),
                        (button) -> {
                            if (this.harvester.getEnergyStored() >= info.mission().getFuelCost()) {
                                CompoundTag data = new CompoundTag();
                                data.putLong(NBTKeys.BLOCK_POS, this.harvester.getBlockPos().asLong());
                                data.putString(NBTKeys.HARVESTER_ACTIVE_MISSION_ID, info.mission().getId().toString());
                                PacketHandler.sendToServer(new CtoSPacket(NBTKeys.ACTION_START_MISSION, data));
                                assert this.minecraft != null;
                                this.minecraft.setScreen(null);
                            }
                            else {
                                System.out.println("Not enough energy!");
                            }
                        }));
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        for (var widget : this.renderables) {
            if (widget instanceof PlanetButton button && button.isHoveredOrFocused()) {
                Mission mission = button.getMission();
                if (mission != null) {
                    renderMissionTooltip(pGuiGraphics, mission, pMouseX, pMouseY);
                    break;
                }
            }
        }
    }

    private void renderMissionTooltip(GuiGraphics pGuiGraphics, Mission mission, int pMouseX, int pMouseY) {
        List<Component> textLines = new ArrayList<>();

        // Add Title
        textLines.add(Component.literal(mission.getName()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
        textLines.add(Component.literal("")); // Spacer

        // Add Fuel Cost
        int fuelCost = mission.getFuelCost();
        int currentFuel = this.harvester.getEnergyStored();
        ChatFormatting fuelColor = (currentFuel >= fuelCost) ? ChatFormatting.GRAY : ChatFormatting.RED;
        textLines.add(Component.literal("Fuel Required: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(fuelCost + " FE").withStyle(fuelColor)));

        Optional<TooltipComponent> visualComponent;
        if (!mission.getRewards().isEmpty()) {
            visualComponent = Optional.of(new ItemListTooltipData(Component.literal("Rewards:"), mission.getRewards()));
        } else {
            visualComponent = Optional.empty();
        }

        pGuiGraphics.renderTooltip(this.font, textLines, visualComponent, pMouseX, pMouseY);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
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
    public boolean isPauseScreen() { return false; }

    private void calculateMissionPositions() {

        List<Mission> missionsToDisplay = MissionManager.getInstance().getAllMissions().entrySet().stream()
                .map(entry -> new Mission(entry.getKey(), entry.getValue()))
                .toList();

        if (missionsToDisplay.isEmpty()) {
            this.missionDisplayInfos = new ArrayList<>();
            return;
        }

        Random random = new Random(this.harvesterData.getSeed());

        int missionCount = missionsToDisplay.size();

        int cols = (int) Math.ceil(Math.sqrt(missionCount));
        int rows = (int) Math.ceil((double) missionCount / cols);

        if (this.width > this.height) {
            cols = Math.max(rows, cols);
            rows = (int) Math.ceil((double) missionCount / cols);
        }
        else {
            rows = Math.max(rows, cols);
            cols = (int) Math.ceil((double) missionCount / rows);
        }

        int cellWidth = (this.width - 2 * SAFE_ZONE_PADDING) / cols;
        int cellHeight = (this.height - 2 * SAFE_ZONE_PADDING) / rows;

        this.missionDisplayInfos = new ArrayList<>();
        for (int i = 0; i < missionCount; i++) {
            Mission mission = missionsToDisplay.get(i);

            int gridRow = i / cols;
            int gridCol = i % cols;

            int cellX = SAFE_ZONE_PADDING + gridCol * cellWidth;
            int cellY = SAFE_ZONE_PADDING + gridRow * cellHeight;

            int xJitterRange = cellWidth - PLANET_TEXTURE_WIDTH;
            int yJitterRange = cellHeight - PLANET_TEXTURE_HEIGHT;

            int xJitter = (xJitterRange > 0) ? random.nextInt(xJitterRange) : 0;
            int yJitter = (yJitterRange > 0) ? random.nextInt(yJitterRange) : 0;

            int finalX = cellX + xJitter;
            int finalY = cellY + yJitter;

            this.missionDisplayInfos.add(new MissionDisplayInfo(mission, finalX, finalY));
        }
    }
}