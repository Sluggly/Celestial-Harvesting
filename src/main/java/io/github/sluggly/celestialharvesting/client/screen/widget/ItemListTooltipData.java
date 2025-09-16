package io.github.sluggly.celestialharvesting.client.screen.widget;

import io.github.sluggly.celestialharvesting.mission.MissionItem;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record ItemListTooltipData(List<MissionItem> items) implements TooltipComponent {}