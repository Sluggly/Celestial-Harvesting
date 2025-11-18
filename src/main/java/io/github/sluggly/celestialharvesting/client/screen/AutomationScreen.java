package io.github.sluggly.celestialharvesting.client.screen;

import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.harvester.HarvesterData;
import io.github.sluggly.celestialharvesting.mission.Mission;
import io.github.sluggly.celestialharvesting.network.CtoSPacket;
import io.github.sluggly.celestialharvesting.network.PacketHandler;
import io.github.sluggly.celestialharvesting.utils.NBTKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AutomationScreen extends Screen {
    private final Harvester harvester;
    private final Screen lastScreen;
    private final int automationTier;

    // Local copies of settings
    private boolean automationOn;
    private int interval;
    private HarvesterData.AutomationMode mode;
    private final List<HarvesterData.AutomationPriority> priorities = new ArrayList<>();
    private ResourceLocation targetMission;

    // UI state variables
    private int intervalIndex;
    private final int[] intervals = {100, 600, 1200}; // 5s, 30s, 1m
    private int targetMissionIndex = -1;
    private final List<Mission> availableMissions;
    private HarvesterData.AutomationPriority selectedPriority = HarvesterData.AutomationPriority.LOWEST_TRAVEL_TIME;

    // UI Widgets
    private Button targetedModeButton;
    private Button cycleTargetButton;
    private Button addPriorityButton;
    private Button clearPrioritiesButton;


    public AutomationScreen(Harvester harvester, Screen lastScreen) {
        super(Component.literal("Harvester Automation"));
        this.harvester = harvester;
        this.lastScreen = lastScreen;
        this.automationTier = harvester.getAutomationTier();

        // Load current settings from the harvester
        HarvesterData data = harvester.getHarvesterData();
        this.automationOn = data.isAutomationOn();
        this.interval = data.getAutomationInterval();
        this.mode = data.getAutomationMode();
        data.getAutomationPriorities().forEach(tag -> this.priorities.add(HarvesterData.AutomationPriority.valueOf(tag.getAsString())));
        this.targetMission = data.getAutomationTarget();

        this.availableMissions = harvester.getHarvesterData().getAvailableMissions().stream()
                .map(Mission::getMissionFromId).filter(Objects::nonNull).collect(Collectors.toList());
        if (this.targetMission != null) {
            for (int i = 0; i < availableMissions.size(); i++) {
                if (availableMissions.get(i).getId().equals(this.targetMission)) {
                    this.targetMissionIndex = i;
                    break;
                }
            }
        }

        for (int i = 0; i < intervals.length; i++) {
            if (intervals[i] == this.interval) this.intervalIndex = i;
        }
    }

    @Override
    protected void init() {
        super.init();
        int left = this.width / 2 - 100;
        int top = this.height / 4;
        int yOffset = 0;

        // --- TIER 1+ ---
        addRenderableWidget(Button.builder(getAutomationStatusComponent(), (btn) -> {
            this.automationOn = !this.automationOn;
            btn.setMessage(getAutomationStatusComponent());
        }).bounds(left, top + yOffset, 200, 20).build());
        yOffset += 24;

        addRenderableWidget(Button.builder(getIntervalComponent(), (btn) -> {
            this.intervalIndex = (this.intervalIndex + 1) % intervals.length;
            this.interval = intervals[this.intervalIndex];
            btn.setMessage(getIntervalComponent());
        }).bounds(left, top + yOffset, 200, 20).build());
        yOffset += 30;

        // --- TIER 2+ ---
        if (automationTier >= 2) {
            // Button to cycle through available priorities
            addRenderableWidget(Button.builder(Component.literal("Cycle Priority"), (btn) -> {
                int nextOrdinal = (this.selectedPriority.ordinal() + 1) % HarvesterData.AutomationPriority.values().length;
                this.selectedPriority = HarvesterData.AutomationPriority.values()[nextOrdinal];
            }).bounds(left, top + yOffset, 98, 20).build());

            // Button to add the currently selected priority to the list
            this.addPriorityButton = addRenderableWidget(Button.builder(Component.literal("Add ->"), (btn) -> {
                if (this.priorities.size() < 3 && !this.priorities.contains(this.selectedPriority)) {
                    this.priorities.add(this.selectedPriority);
                }
            }).bounds(left + 102, top + yOffset, 48, 20).build());

            // Button to clear the list
            this.clearPrioritiesButton = addRenderableWidget(Button.builder(Component.literal("Clear"), (btn) -> {
                this.priorities.clear();
            }).bounds(left + 152, top + yOffset, 48, 20).build());

            yOffset += 50; // Extra space for drawing the list
        }

        // --- TIER 3+ ---
        if (automationTier >= 3) {
            this.targetedModeButton = addRenderableWidget(Button.builder(getTargetedModeComponent(), (btn) -> {
                if (this.mode == HarvesterData.AutomationMode.TARGETED) {
                    this.mode = HarvesterData.AutomationMode.PRIORITY; // Or RANDOM if tier is < 2
                } else {
                    this.mode = HarvesterData.AutomationMode.TARGETED;
                }
                btn.setMessage(getTargetedModeComponent());
                updateWidgetStates();
            }).bounds(left, top + yOffset, 200, 20).build());
            yOffset += 24;

            this.cycleTargetButton = addRenderableWidget(Button.builder(getTargetMissionComponent(), (btn) -> {
                if (!this.availableMissions.isEmpty()) {
                    this.targetMissionIndex = (this.targetMissionIndex + 1) % this.availableMissions.size();
                    this.targetMission = this.availableMissions.get(this.targetMissionIndex).getId();
                    btn.setMessage(getTargetMissionComponent());
                }
            }).bounds(left, top + yOffset, 200, 20).build());
        }

        // Bottom Buttons
        addRenderableWidget(Button.builder(Component.literal("Save and Close"), (btn) -> saveAndClose()).bounds(left, this.height - 50, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), (btn) -> this.minecraft.setScreen(this.lastScreen)).bounds(left + 102, this.height - 50, 98, 20).build());

        updateWidgetStates();
    }

    private void updateWidgetStates() {
        if (automationTier >= 2) {
            boolean isPriorityMode = this.mode != HarvesterData.AutomationMode.TARGETED;
            this.addPriorityButton.active = isPriorityMode;
            this.clearPrioritiesButton.active = isPriorityMode;
        }
        if (automationTier >= 3) {
            this.cycleTargetButton.active = this.mode == HarvesterData.AutomationMode.TARGETED;
        }
    }

    private void saveAndClose() {
        CompoundTag data = new CompoundTag();
        data.putLong(NBTKeys.BLOCK_POS, this.harvester.getBlockPos().asLong());
        data.putBoolean(NBTKeys.HARVESTER_AUTOMATION_ON, this.automationOn);
        data.putInt(NBTKeys.HARVESTER_AUTOMATION_INTERVAL, this.interval);
        data.putString(NBTKeys.HARVESTER_AUTOMATION_MODE, this.mode.name());

        ListTag prioritiesTag = new ListTag();
        for (HarvesterData.AutomationPriority p : this.priorities) {
            prioritiesTag.add(StringTag.valueOf(p.name()));
        }
        data.put(NBTKeys.HARVESTER_AUTOMATION_PRIORITIES, prioritiesTag);

        if (this.targetMission != null) {
            data.putString(NBTKeys.HARVESTER_AUTOMATION_TARGET, this.targetMission.toString());
        }

        PacketHandler.sendToServer(new CtoSPacket(NBTKeys.ACTION_SET_AUTOMATION_CONFIG, data));
        assert this.minecraft != null;
        this.minecraft.setScreen(this.lastScreen);
    }

    private Component getAutomationStatusComponent() {
        return Component.literal("Automation: " + (this.automationOn ? "ON" : "OFF"));
    }

    private Component getIntervalComponent() {
        return Component.literal("Check Interval: " + (this.interval / 20) + "s");
    }

    private Component getTargetedModeComponent() {
        return Component.literal("Mode: " + (this.mode == HarvesterData.AutomationMode.TARGETED ? "Targeted" : "Priority/Random"));
    }

    private Component getTargetMissionComponent() {
        if (this.targetMissionIndex != -1) {
            return Component.literal("Target: ").append(this.availableMissions.get(this.targetMissionIndex).getName());
        }
        return Component.literal("Target: None");
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Render Priority List
        if (automationTier >= 2) {
            int left = this.width / 2 - 100;
            int top = this.height / 4 + 78;

            // Draw the currently selected priority that can be added
            guiGraphics.drawString(this.font, "Selected: ", left, top, 0xAAAAAA);
            guiGraphics.drawString(this.font, this.selectedPriority.getDisplayName(), left + 45, top, 0x55FFFF); // Aqua color

            // Draw the active priority list
            guiGraphics.drawString(this.font, "Active Priorities (Max 3):", left, top + 12, 0xAAAAAA);
            for (int i = 0; i < this.priorities.size(); i++) {
                guiGraphics.drawString(this.font, (i + 1) + ". " + this.priorities.get(i).getDisplayName().getString(), left + 4, top + 24 + (i * 10), 0xFFFFFF);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}