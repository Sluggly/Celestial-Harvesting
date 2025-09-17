package io.github.sluggly.celestialharvesting.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
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

public class HarvesterRenderer implements BlockEntityRenderer<Harvester> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/entity/harvester.png");
    public static final ResourceLocation PAD_TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/entity/landing_pad.png");
    private final HarvesterModel model;
    private final LandingPadModel landingPadModel;

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
            this.model.renderToBuffer(pPoseStack, pBufferSource.getBuffer(RenderType.entityCutout(TEXTURE)), pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);

            pPoseStack.popPose();
        }
    }
}