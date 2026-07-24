package com.chambered.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Renders first-person player arms at gun bone anchors ({@code right_arm} / {@code left_arm}).
 * <p>
 * Orientation comes from the bone in Blockbench (pivot + rotation). Code only snaps the
 * vanilla arm's hand onto the bone origin — no extra yaw/pitch/roll.
 * <p>
 * Blockbench tip: put the bone pivot at the wrist; rotate the bone until the forearm
 * points the way you want. Vanilla arm mesh has the hand at local {@code y = +10/16}.
 */
public final class GunArmRenderer {
	private static final float HAND_Y = 10.0f / 16.0f;
	private static final float ARM_SCALE = 1f;

	private GunArmRenderer() {
	}

	public static void renderAtBone(
			PoseStack poseStack,
			SubmitNodeCollector collector,
			int packedLight,
			HumanoidArm arm
	) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || player.isInvisible()) {
			return;
		}

		AvatarRenderer<AbstractClientPlayer> avatarRenderer = client.getEntityRenderDispatcher().getPlayerRenderer(player);
		PlayerModel model = avatarRenderer.getModel();
		ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
		ModelPart sleevePart = arm == HumanoidArm.RIGHT ? model.rightSleeve : model.leftSleeve;
		boolean showSleeve = arm == HumanoidArm.RIGHT
				? player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)
				: player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);

		zeroPart(armPart);
		zeroPart(sleevePart);
		armPart.visible = true;
		sleevePart.visible = showSleeve;

		Identifier skin = player.getSkin().body().texturePath();
		var renderType = RenderTypes.entityTranslucent(skin);

		poseStack.pushPose();
		poseStack.scale(ARM_SCALE, ARM_SCALE, ARM_SCALE);
		// Hand → bone origin; leave rotation to the geo bone.
		poseStack.translate(0.0f, -HAND_Y, 0.0f);

		collector.submitModelPart(armPart, poseStack, renderType, packedLight, OverlayTexture.NO_OVERLAY, null);
		if (showSleeve) {
			collector.submitModelPart(sleevePart, poseStack, renderType, packedLight, OverlayTexture.NO_OVERLAY, null);
		}
		poseStack.popPose();
	}

	private static void zeroPart(ModelPart part) {
		part.resetPose();
		part.x = 0.0f;
		part.y = 0.0f;
		part.z = 0.0f;
		part.xRot = 0.0f;
		part.yRot = 0.0f;
		part.zRot = 0.0f;
	}
}
