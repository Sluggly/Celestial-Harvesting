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
	public final ModelPart tires;
	public final ModelPart body;
	public final ModelPart shields;
	public final ModelPart solar;
	public final ModelPart thrusters;
	public final ModelPart loot;
	public final ModelPart comms;
	public final ModelPart inventory;
	public final ModelPart root;

	public HarvesterModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
		this.tires = root.getChild("tires");
		this.body = root.getChild("body");
		this.shields = root.getChild("shields");
		this.solar = root.getChild("solar");
		this.thrusters = root.getChild("thrusters");
		this.loot = root.getChild("loot");
		this.comms = root.getChild("comms");
		this.inventory = root.getChild("inventory");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition tires = partdefinition.addOrReplaceChild("tires", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(32, 17).addBox(-1.0F, -3.0F, 10.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(28, 28).addBox(9.0F, -3.0F, 10.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(18, 28).addBox(9.0F, -3.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 24.0F, -6.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -5.0F, -1.0F, 8.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 23.0F, -5.0F));

		PartDefinition shields = partdefinition.addOrReplaceChild("shields", CubeListBuilder.create().texOffs(10, 34).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(30, 34).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(20, 34).addBox(9.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(36, 34).addBox(9.0F, 0.0F, 0.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 20.0F, -1.0F));

		PartDefinition solar = partdefinition.addOrReplaceChild("solar", CubeListBuilder.create().texOffs(38, 27).addBox(1.0F, -10.0F, 3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 17).addBox(-3.0F, -11.0F, -5.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition thrusters = partdefinition.addOrReplaceChild("thrusters", CubeListBuilder.create().texOffs(30, 38).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(36, 38).addBox(-3.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 22.0F, 7.0F));

		PartDefinition loot = partdefinition.addOrReplaceChild("loot", CubeListBuilder.create().texOffs(32, 23).addBox(-3.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 21.0F, -7.0F));

		PartDefinition comms = partdefinition.addOrReplaceChild("comms", CubeListBuilder.create().texOffs(10, 39).addBox(-3.0F, -5.0F, 6.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 39).addBox(-3.0F, -8.0F, 7.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition inventory = partdefinition.addOrReplaceChild("inventory", CubeListBuilder.create().texOffs(0, 28).addBox(0.0F, -4.0F, -13.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 19.0F, 8.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}