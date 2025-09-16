package io.github.sluggly.celestialharvesting.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import io.github.sluggly.celestialharvesting.client.model.HarvesterModel;
import io.github.sluggly.celestialharvesting.harvester.Harvester;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HarvesterRenderer implements BlockEntityRenderer<Harvester> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(CelestialHarvesting.MOD_ID, "textures/entity/harvester.png");
    private final HarvesterModel model;

    public HarvesterRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new HarvesterModel(context.bakeLayer(HarvesterModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull Harvester pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();

        pPoseStack.translate(0.5, 1.5, 0.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));

        this.model.renderToBuffer(pPoseStack, pBufferSource.getBuffer(RenderType.entityCutout(TEXTURE)), pPackedLight, pPackedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);

        pPoseStack.popPose();
    }
}