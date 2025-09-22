package io.github.sluggly.celestialharvesting.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.model.HarvesterModel;
import io.github.sluggly.celestialharvesting.client.model.LandingPadModel;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import io.github.sluggly.celestialharvesting.harvester.HarvesterBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HarvesterRenderer implements BlockEntityRenderer<Harvester> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/entity/harvester.png");
    public static final ResourceLocation PAD_TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/entity/landing_pad.png");
    private final HarvesterModel model;
    private final LandingPadModel landingPadModel;

    // Tiers
    private static final ResourceLocation TIER_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "tier_2_drive");
    private static final ResourceLocation TIER_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "tier_3_singularity_drive");
    // Speed
    private static final ResourceLocation SPEED_1 = new ResourceLocation(CelestialHarvesting.MOD_ID, "speed_upgrade_1");
    private static final ResourceLocation SPEED_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "speed_upgrade_2");
    private static final ResourceLocation SPEED_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "speed_upgrade_3");
    // Shields
    private static final ResourceLocation SHIELD_1 = new ResourceLocation(CelestialHarvesting.MOD_ID, "shield_upgrade_1");
    private static final ResourceLocation SHIELD_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "shield_upgrade_2");
    private static final ResourceLocation SHIELD_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "shield_upgrade_3");
    // Solar
    private static final ResourceLocation SOLAR_1 = new ResourceLocation(CelestialHarvesting.MOD_ID, "solar_panel_1");
    private static final ResourceLocation SOLAR_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "solar_panel_2");
    private static final ResourceLocation SOLAR_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "solar_panel_3");
    // Loot
    private static final ResourceLocation LOOT_1 = new ResourceLocation(CelestialHarvesting.MOD_ID, "loot_upgrade_1");
    private static final ResourceLocation LOOT_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "loot_upgrade_2");
    private static final ResourceLocation LOOT_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "loot_upgrade_3");
    // Comms
    private static final ResourceLocation COMMS_1 = new ResourceLocation(CelestialHarvesting.MOD_ID, "comms_upgrade_1");
    private static final ResourceLocation COMMS_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "comms_upgrade_2");
    private static final ResourceLocation COMMS_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "comms_upgrade_3");
    // Inventory
    private static final ResourceLocation INV_1 = new ResourceLocation(CelestialHarvesting.MOD_ID, "inventory_upgrade_1");
    private static final ResourceLocation INV_2 = new ResourceLocation(CelestialHarvesting.MOD_ID, "inventory_upgrade_2");
    private static final ResourceLocation INV_3 = new ResourceLocation(CelestialHarvesting.MOD_ID, "inventory_upgrade_3");

    public HarvesterRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new HarvesterModel(context.bakeLayer(HarvesterModel.LAYER_LOCATION));
        this.landingPadModel = new LandingPadModel(context.bakeLayer(LandingPadModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull Harvester pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Harvester.AnimationState animState = pBlockEntity.getAnimationState();
        BlockState blockState = pBlockEntity.getBlockState();

        if (blockState.getValue(HarvesterBlock.STATE) == HarvesterBlock.State.IN_MISSION || animState != Harvester.AnimationState.NONE) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.5, 0.5);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
            this.landingPadModel.renderToBuffer(pPoseStack, pBufferSource.getBuffer(RenderType.entityCutout(PAD_TEXTURE)), pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            pPoseStack.popPose();
        }

        boolean shouldRenderShip = (blockState.getValue(HarvesterBlock.STATE) == HarvesterBlock.State.IDLE && animState == Harvester.AnimationState.NONE)
                || animState == Harvester.AnimationState.TAKING_OFF
                || animState == Harvester.AnimationState.LANDING;

        if (shouldRenderShip) {
            pPoseStack.pushPose();
            float yOffset = pBlockEntity.getAnimationYOffset(pPartialTick);
            pPoseStack.translate(0, yOffset, 0);
            pPoseStack.translate(0.5, 1.5, 0.5);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(180));

            Set<ResourceLocation> upgrades = pBlockEntity.getHarvesterData().getUnlockedUpgrades();
            VertexConsumer buffer = pBufferSource.getBuffer(RenderType.entityCutout(TEXTURE));

            float[] silver = {0.75f, 0.75f, 0.75f};
            float[] gold = {1.0f, 0.84f, 0.0f};

            float[] bodyTint = {1f, 1f, 1f};
            if (upgrades.contains(TIER_3)) bodyTint = gold;
            else if (upgrades.contains(TIER_2)) bodyTint = silver;
            this.model.body.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, bodyTint[0], bodyTint[1], bodyTint[2], 1f);

            float[] tireTint = {1f, 1f, 1f};
            if (upgrades.contains(SPEED_3)) tireTint = gold;
            else if (upgrades.contains(SPEED_2)) tireTint = silver;
            this.model.tires.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tireTint[0], tireTint[1], tireTint[2], 1f);

            if (upgrades.contains(SHIELD_1) || upgrades.contains(SHIELD_2) || upgrades.contains(SHIELD_3)) {
                float[] tint = {1f, 1f, 1f};
                if (upgrades.contains(SHIELD_3)) tint = gold;
                else if (upgrades.contains(SHIELD_2)) tint = silver;
                this.model.shields.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tint[0], tint[1], tint[2], 1f);
            }

            if (upgrades.contains(SOLAR_1) || upgrades.contains(SOLAR_2) || upgrades.contains(SOLAR_3)) {
                float[] tint = {1f, 1f, 1f};
                if (upgrades.contains(SOLAR_3)) tint = gold;
                else if (upgrades.contains(SOLAR_2)) tint = silver;
                this.model.solar.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tint[0], tint[1], tint[2], 1f);
            }

            if (upgrades.contains(SPEED_1) || upgrades.contains(SPEED_2) || upgrades.contains(SPEED_3)) {
                float[] tint = {1f, 1f, 1f};
                if (upgrades.contains(SPEED_3)) tint = gold;
                else if (upgrades.contains(SPEED_2)) tint = silver;
                this.model.thrusters.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tint[0], tint[1], tint[2], 1f);
            }

            if (upgrades.contains(LOOT_1) || upgrades.contains(LOOT_2) || upgrades.contains(LOOT_3)) {
                float[] tint = {1f, 1f, 1f};
                if (upgrades.contains(LOOT_3)) tint = gold;
                else if (upgrades.contains(LOOT_2)) tint = silver;
                this.model.loot.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tint[0], tint[1], tint[2], 1f);
            }

            if (upgrades.contains(COMMS_1) || upgrades.contains(COMMS_2) || upgrades.contains(COMMS_3)) {
                float[] tint = {1f, 1f, 1f};
                if (upgrades.contains(COMMS_3)) tint = gold;
                else if (upgrades.contains(COMMS_2)) tint = silver;
                this.model.comms.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tint[0], tint[1], tint[2], 1f);
            }

            if (upgrades.contains(INV_1) || upgrades.contains(INV_2) || upgrades.contains(INV_3)) {
                float[] tint = {1f, 1f, 1f};
                if (upgrades.contains(INV_3)) tint = gold;
                else if (upgrades.contains(INV_2)) tint = silver;
                this.model.inventory.render(pPoseStack, buffer, pPackedLight, pPackedOverlay, tint[0], tint[1], tint[2], 1f);
            }

            pPoseStack.popPose();
        }
    }
}