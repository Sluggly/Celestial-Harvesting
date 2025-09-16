package io.github.sluggly.celestialharvesting.client.screen.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class UpgradeButton extends Button {
    public final ResourceLocation upgradeId;

    public UpgradeButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation upgradeId, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, DEFAULT_NARRATION);
        this.upgradeId = upgradeId;
    }
}