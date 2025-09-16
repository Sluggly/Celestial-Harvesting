package io.github.sluggly.celestialharvesting.client.data;

import io.github.sluggly.celestialharvesting.client.screen.widget.UpgradeButton;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.mission.MissionItem;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeDefinition;
import io.github.sluggly.celestialharvesting.upgrade.UpgradeManager;
import io.github.sluggly.celestialharvesting.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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
        public final Component tooltip;

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
            this.tooltip = buildTooltip(this, player);
        }

        private static Component buildTooltip(UpgradeDisplayInfo info, Player player) {
            // Determine color based on row
            int color = info.def.row() < 2 ? 0xFFFFFF : (info.def.row() < 4 ? 0x00FF00 : (info.def.row() < 6 ? 0x00BFFF : 0xFF0000));

            StringBuilder tooltipText = new StringBuilder();
            tooltipText.append("\n\n").append(info.def.description());

            if (info.isUnlocked) {
                tooltipText.append("\n\n§aINSTALLED!");
            } else {
                if (!info.hasUpgradeRequirements) {
                    tooltipText.append("\n\n§cRequired Upgrades:");
                    for (String reqName : info.def.requirements()) {
                        tooltipText.append("\n - ").append(reqName); // You can make this fancier later
                    }
                }

                tooltipText.append("\n\n§6Cost:");
                for (MissionItem item : info.def.cost()) {
                    tooltipText.append("\n - ").append(item.count()).append("x ").append(item.item().getDescription().getString());
                }

                if (!info.hasItemRequirements) {
                    tooltipText.append("\n\n§cMissing Items:");
                    for (MissionItem item : info.def.cost()) {
                        int owned = Utils.countItemInInventory(item.item().asItem(), player);
                        int needed = item.count();
                        if (owned < needed) {
                            tooltipText.append(String.format("\n- %d %s", needed - owned, item.item().getDescription().getString()));
                        }
                    }
                }
            }

            Component title = Component.literal(info.def.name()).withStyle(Style.EMPTY.withColor(color));
            return title.copy().append(Component.literal(tooltipText.toString()));
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