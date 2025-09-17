package io.github.sluggly.celestialharvesting.client.screen.widget;

import io.github.sluggly.celestialharvesting.mission.MissionItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemListTooltipComponent implements ClientTooltipComponent {
    private final Component title;
    private final List<MissionItem> items;
    private static final int ROW_HEIGHT = 18;
    private static final int ICON_SIZE = 16;

    public ItemListTooltipComponent(ItemListTooltipData data) {
        this.title = data.title();
        this.items = data.items();
    }

    @Override
    public int getHeight() {
        return 10 + (items.size() * ROW_HEIGHT);
    }

    @Override
    public int getWidth(Font pFont) {
        int maxWidth = pFont.width(this.title);
        for (MissionItem item : items) {
            String text;
            if (item.minimum().isPresent()) { text = "- " + item.minimum().get() + " to " + item.count() + " " + item.item().getDescription().getString(); }
            else { text = "- " + item.count() + "x " + item.item().getDescription().getString(); }

            if (item.chance().isPresent() && item.chance().get() < 1.0) { text += String.format(" (%.0f%%)", item.chance().get() * 100); }
            int width = pFont.width(text) + ICON_SIZE + 4;
            if (width > maxWidth) { maxWidth = width; }
        }
        return maxWidth;
    }

    @Override
    public void renderImage(@NotNull Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        pGuiGraphics.drawString(pFont, this.title, pX, pY, 0xFFA0A0A0, false);
        pY += 10;

        for (MissionItem item : items) {
            Component itemText;

            if (item.minimum().isPresent()) { itemText = Component.literal("- " + item.minimum().get() + " to " + item.count() + " " + item.item().getDescription().getString()); }
            else { itemText = Component.literal("- " + item.count() + "x " + item.item().getDescription().getString()); }

            if (item.chance().isPresent() && item.chance().get() < 1.0) {
                String chanceString = String.format(" (%.0f%%)", item.chance().get() * 100);
                itemText = itemText.copy().append(Component.literal(chanceString).withStyle(ChatFormatting.GRAY));
            }

            pGuiGraphics.drawString(pFont, itemText, pX, pY + 4, 0xFFFFFFFF, false);

            ItemStack stack = new ItemStack(item.item(), 1);
            int textWidth = pFont.width(itemText);
            pGuiGraphics.renderFakeItem(stack, pX + textWidth + 4, pY);

            pY += ROW_HEIGHT;
        }
    }
}