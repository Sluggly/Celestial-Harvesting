package io.github.sluggly.celestialharvesting.client.data;

import io.github.sluggly.celestialharvesting.client.screen.widget.ItemListTooltipData;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.mission.MissionItem;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeDefinition;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeManager;
import io.github.sluggly.celestialharvesting.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UpgradeData {
    public final Map<ResourceLocation, UpgradeDisplayInfo> upgradeInfoMap = new LinkedHashMap<>();

    public static class UpgradeDisplayInfo {
        public final ResourceLocation id;
        public final UpgradeDefinition def;
        public final ItemStack icon;
        public final boolean isUnlocked;
        public final boolean hasItemRequirements;
        public final boolean hasUpgradeRequirements;
        public final boolean canUnlock;
        public List<Component> textTooltip;
        public Optional<TooltipComponent> visualTooltip;

        public UpgradeDisplayInfo(ResourceLocation id, UpgradeDefinition def, Harvester harvester, Player player) {
            this.id = id;
            this.def = def;
            this.icon = new ItemStack(def.icon());
            this.isUnlocked = harvester.getHarvesterData().hasUpgrade(id);

            // Check upgrade requirements
            boolean reqsMet = true;
            for (String reqName : def.requirements()) {
                if (!harvester.getHarvesterData().hasUpgrade(new ResourceLocation(reqName))) {
                    reqsMet = false;
                    break;
                }
            }
            this.hasUpgradeRequirements = reqsMet;

            // Check item requirements
            boolean itemsMet = true;
            for (MissionItem item : def.cost()) {
                if (!Utils.hasEnoughItems(item.item().asItem(), player, item.count())) {
                    itemsMet = false;
                    break;
                }
            }
            this.hasItemRequirements = itemsMet;
            this.canUnlock = !isUnlocked && hasUpgradeRequirements && hasItemRequirements;
            buildTooltip(this, harvester, player);
        }

        private void buildTooltip(UpgradeDisplayInfo info, Harvester harvester, Player player) {
            List<Component> textLines = new ArrayList<>();
            int color = info.def.row() < 2 ? 0xFFFFFF : (info.def.row() < 4 ? 0x00FF00 : (info.def.row() < 6 ? 0x00BFFF : 0xFF0000));

            // Add Title and Description
            textLines.add(Component.literal(info.def.name()).withStyle(Style.EMPTY.withColor(color)));
            textLines.add(Component.literal(info.def.description()).withStyle(Style.EMPTY.withColor(0xAAAAAA)));

            info.def.speed_modifier().ifPresent(modifier -> {
                int percentage = (int) ((1.0f - modifier) * 100);
                textLines.add(Component.literal("")); // Spacer
                textLines.add(Component.literal("Reduces mission time by " + percentage + "%")
                        .withStyle(Style.EMPTY.withColor(0x55FFFF))); // Aqua color
            });

            if (info.isUnlocked) {
                textLines.add(Component.literal("")); // Spacer
                textLines.add(Component.literal("INSTALLED!").withStyle(Style.EMPTY.withColor(0x55FF55)));
            } else {
                // Add Requirements
                if (!info.hasUpgradeRequirements) {
                    textLines.add(Component.literal("")); // Spacer
                    textLines.add(Component.literal("Required Upgrades:").withStyle(Style.EMPTY.withColor(0xFF5555)));
                    for (String reqIdStr : info.def.requirements()) {
                        ResourceLocation reqId = new ResourceLocation(reqIdStr);
                        UpgradeDefinition reqDef = UpgradeManager.getInstance().getAllUpgrades().get(reqId);
                        String reqName = reqDef != null ? reqDef.name() : reqIdStr;
                        textLines.add(Component.literal(" - " + reqName).withStyle(Style.EMPTY.withColor(0xFF5555)));
                    }
                }
            }

            // Assign the text part of the tooltip
            this.textTooltip = textLines;

            if (!info.def.cost().isEmpty() && !info.isUnlocked) {
                this.visualTooltip = Optional.of(new ItemListTooltipData(Component.literal("Cost:"), info.def.cost()));
            } else {
                this.visualTooltip = Optional.empty();
            }
        }
    }

    public UpgradeData(@NotNull Harvester harvester) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Map<ResourceLocation, UpgradeDefinition> allUpgrades = UpgradeManager.getInstance().getAllUpgrades();

        // Use a sorted list to ensure a consistent order
        List<ResourceLocation> sortedIds = new ArrayList<>(allUpgrades.keySet());
        sortedIds.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation id : sortedIds) {
            UpgradeDefinition def = allUpgrades.get(id);
            if (def != null) {
                upgradeInfoMap.put(id, new UpgradeDisplayInfo(id, def, harvester, player));
            }
        }
    }
}