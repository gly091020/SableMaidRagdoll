package com.gly091020.SableMaidRagdoll.client.model;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MaidDollDefaultModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "maid_doll_default"), "main");
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart legLeft;
	private final ModelPart legRight;
	private final ModelPart armLeft;
	private final ModelPart armLeftPositioningBone;
	private final ModelPart armRight;
	private final ModelPart armRightPositioningBone;
	private final ModelPart backpackPositioningBone;

	public MaidDollDefaultModel(ModelPart root) {
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.legLeft = root.getChild("legLeft");
		this.legRight = root.getChild("legRight");
		this.armLeft = root.getChild("armLeft");
		this.armLeftPositioningBone = this.armLeft.getChild("armLeftPositioningBone");
		this.armRight = root.getChild("armRight");
		this.armRightPositioningBone = this.armRight.getChild("armRightPositioningBone");
		this.backpackPositioningBone = root.getChild("backpackPositioningBone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -5.75F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(2.0F))
		.texOffs(16, 0).addBox(-2.0F, -5.75F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(2.1F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(8, 8).addBox(-2.0F, -7.5F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(1.0F))
		.texOffs(8, 16).addBox(-2.0F, -7.5F, -1.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(1.1F)), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition legLeft = partdefinition.addOrReplaceChild("legLeft", CubeListBuilder.create().texOffs(8, 24).addBox(-1.25F, 0.25F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.5F))
		.texOffs(0, 24).addBox(-1.25F, 0.25F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.6F)), PartPose.offset(1.8F, 17.0F, 0.0F));

		PartDefinition legRight = partdefinition.addOrReplaceChild("legRight", CubeListBuilder.create().texOffs(0, 8).addBox(-0.75F, 0.25F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.5F))
		.texOffs(0, 16).addBox(-0.75F, 0.25F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.6F)), PartPose.offset(-1.8F, 17.0F, 0.0F));

		PartDefinition armLeft = partdefinition.addOrReplaceChild("armLeft", CubeListBuilder.create().texOffs(16, 24).addBox(0.75F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.5F))
		.texOffs(24, 24).addBox(0.75F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.6F)), PartPose.offset(3.0F, 9.0F, 0.0F));

		PartDefinition armLeftPositioningBone = armLeft.addOrReplaceChild("armLeftPositioningBone", CubeListBuilder.create(), PartPose.offset(1.75F, 5.75F, 0.0F));

		PartDefinition armRight = partdefinition.addOrReplaceChild("armRight", CubeListBuilder.create().texOffs(20, 8).addBox(-2.75F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.5F))
		.texOffs(20, 16).addBox(-2.75F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.6F)), PartPose.offset(-3.0F, 9.0F, 0.0F));

		PartDefinition armRightPositioningBone = armRight.addOrReplaceChild("armRightPositioningBone", CubeListBuilder.create(), PartPose.offset(-1.75F, 5.75F, 0.0F));

		PartDefinition backpackPositioningBone = partdefinition.addOrReplaceChild("backpackPositioningBone", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		legLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		legRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		armLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		armRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		backpackPositioningBone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
