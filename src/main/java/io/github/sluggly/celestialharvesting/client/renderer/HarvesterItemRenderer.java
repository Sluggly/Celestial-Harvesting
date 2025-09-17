package io.github.sluggly.celestialharvesting.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sluggly.celestialharvesting.client.model.HarvesterModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HarvesterItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final ModelPart root;
    private final HarvesterModel model;

    public HarvesterItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.root = Minecraft.getInstance().getEntityModels().bakeLayer(HarvesterModel.LAYER_LOCATION);
        this.model = new HarvesterModel(this.root);
    }

    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();

        pPoseStack.translate(0.5, 1.5, 0.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));

        this.model.renderToBuffer(pPoseStack, pBuffer.getBuffer(RenderType.entityCutout(HarvesterRenderer.TEXTURE)), pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        pPoseStack.popPose();
    }
}