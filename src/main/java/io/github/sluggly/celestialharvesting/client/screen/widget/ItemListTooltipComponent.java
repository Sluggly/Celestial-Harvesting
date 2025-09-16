package io.github.sluggly.celestialharvesting.client.screen.widget;

import io.github.sluggly.celestialharvesting.mission.MissionItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemListTooltipComponent implements ClientTooltipComponent {
    private final List<MissionItem> items;
    private static final int ROW_HEIGHT = 18; // Height for each item row
    private static final int ICON_SIZE = 16;  // Standard item icon size

    public ItemListTooltipComponent(ItemListTooltipData data) { this.items = data.items(); }

    @Override
    public int getHeight() { return 10 + (items.size() * ROW_HEIGHT); }

    @Override
    public int getWidth(@NotNull Font pFont) {
        int maxWidth = 0;
        for (MissionItem item : items) {
            int width = pFont.width("- " + item.count() + "x " + item.item().getDescription().getString()) + ICON_SIZE + 4;
            if (width > maxWidth) { maxWidth = width; }
        }
        return maxWidth;
    }

    @Override
    public void renderImage(@NotNull Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        pGuiGraphics.drawString(pFont, Component.literal("Cost:"), pX, pY, 0xFFA0A0A0, false); // Gray title
        pY += 10;

        for (MissionItem item : items) {
            String text = "- " + item.count() + "x " + item.item().getDescription().getString();
            pGuiGraphics.drawString(pFont, text, pX, pY + 4, 0xFFFFFFFF, false); // White text

            ItemStack stack = new ItemStack(item.item(), 1);
            int textWidth = pFont.width(text);
            pGuiGraphics.renderFakeItem(stack, pX + textWidth + 4, pY);

            pY += ROW_HEIGHT;
        }
    }
}