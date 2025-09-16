package io.github.sluggly.celestialharvesting.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sluggly.celestialharvesting.CelestialHarvesting;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HarvesterModel extends Model {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(CelestialHarvesting.MOD_ID, "harvester"), "main");
	private final ModelPart bb_main;

	public HarvesterModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(22, 17).addBox(4.0F, -3.0F, -7.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -6.0F, -6.0F, 8.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(22, 23).addBox(4.0F, -3.0F, 4.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-6.0F, -3.0F, 4.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(10, 25).addBox(-6.0F, -3.0F, -7.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}