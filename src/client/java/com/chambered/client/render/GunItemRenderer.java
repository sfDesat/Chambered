package com.chambered.client.render;

import com.chambered.attach.SlotType;
import com.chambered.client.feel.AdsState;
import com.chambered.client.ui.FieldAttachBoneProjector;
import com.chambered.client.ui.FieldAttachPreview;
import com.chambered.component.BoltState;
import com.chambered.component.GunState;
import com.chambered.data.GunDefinition;
import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.registry.ModComponents;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/**
 * Gun renderer: ADS pose, slide/mag from {@link GunState}, FP arms at bone anchors.
 * Pose offsets come from each gun's {@code visual} definition block.
 */
public final class GunItemRenderer extends GeoItemRenderer<GunItem> {
	public static final DataTicket<GunState> GUN_STATE = DataTicket.create("chambered_gun_state", GunState.class);
	public static final DataTicket<GunDefinition.VisualSpec> GUN_VISUAL =
			DataTicket.create("chambered_gun_visual", GunDefinition.VisualSpec.class);

	public static final String RIGHT_ARM_BONE = "right_arm";
	public static final String LEFT_ARM_BONE = "left_arm";

	public GunItemRenderer(GunItem item) {
		Identifier id = item.getDefinitionId();
		super(
				new DefaultedItemGeoModel<GunItem>(id)
						.withAltTexture(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_geo"))
		);
	}

	@Override
	public void captureDefaultRenderState(
			GunItem animatable,
			RenderData data,
			GeoRenderState renderState,
			float partialTick
	) {
		super.captureDefaultRenderState(animatable, data, renderState, partialTick);
		ItemStack stack = data.itemStack();
		GunState state = stack.getOrDefault(ModComponents.GUN_STATE, GunState.DEFAULT);
		renderState.addGeckolibData(GUN_STATE, state);
		GunDefinition.VisualSpec visual = GunDefinitions.gun(animatable.getDefinitionId())
				.map(GunDefinition::visual)
				.orElse(GunDefinition.VisualSpec.DEFAULT);
		renderState.addGeckolibData(GUN_VISUAL, visual);
	}

	@Override
	public void preRenderPass(RenderPassInfo<GeoRenderState> renderInfo, SubmitNodeCollector renderCallback) {
		super.preRenderPass(renderInfo, renderCallback);
		GunState state = renderInfo.getOrDefaultGeckolibData(GUN_STATE, GunState.DEFAULT);
		GunDefinition.VisualSpec visual = renderInfo.getOrDefaultGeckolibData(GUN_VISUAL, GunDefinition.VisualSpec.DEFAULT);

		boolean slideBack = state.bolt() == BoltState.LOCKED_BACK
				|| (!state.hasMagazine() && !state.isChambered());
		boolean magSeated = state.hasMagazine();

		renderInfo.addBoneUpdater((info, snapshots) -> {
			// Field-attach menu: pin the gun to bind pose so rail dots match the mesh
			// (idle anim would move cubes while mount pivots stay static / pixel-snapped).
			if (FieldAttachPreview.isActive()) {
				snapshots.ifPresent("gun", snap -> {
					snap.setRotation(0.0f, 0.0f, 0.0f);
					snap.setTranslation(0.0f, 0.0f, 0.0f);
				});
			}
			snapshots.ifPresent("slide", snap -> {
				if (slideBack) {
					snap.setTranslateZ(visual.slideLockedZ());
				}
			});
			snapshots.ifPresent("magazine", snap -> {
				if (!magSeated) {
					snap.skipRender(true);
				}
			});
			for (SlotType slot : SlotType.values()) {
				snapshots.ifPresent(slot.boneName(), snap -> {
					if (!state.attachments().containsKey(slot)) {
						snap.skipRender(true);
					}
				});
			}
		});

		ItemDisplayContext perspective = renderInfo.getOrDefaultGeckolibData(
				DataTickets.ITEM_RENDER_PERSPECTIVE,
				ItemDisplayContext.NONE
		);
		if (!perspective.firstPerson()) {
			return;
		}

		attachArm(renderInfo, RIGHT_ARM_BONE, HumanoidArm.RIGHT);
		attachArm(renderInfo, LEFT_ARM_BONE, HumanoidArm.LEFT);
	}

	private static void attachArm(
			RenderPassInfo<GeoRenderState> renderInfo,
			String boneName,
			HumanoidArm arm
	) {
		renderInfo.model().getBone(boneName).ifPresent(bone ->
				renderInfo.addPerBoneRender(bone, (info, geoBone, collector) ->
						GunArmRenderer.renderAtBone(info.poseStack(), collector, info.packedLight(), arm)
				)
		);
	}

	@Override
	public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderInfo) {
		super.adjustRenderPose(renderInfo);

		ItemDisplayContext perspective = renderInfo.getOrDefaultGeckolibData(
				DataTickets.ITEM_RENDER_PERSPECTIVE,
				ItemDisplayContext.NONE
		);

		// Field-attach FIXED preview: face barrel left without a 2D negative scale (that culls the blit).
		if (FieldAttachPreview.isActive() && perspective == ItemDisplayContext.FIXED) {
			renderInfo.poseStack().mulPose(Axis.YP.rotationDegrees(FieldAttachBoneProjector.PREVIEW_YAW_DEGREES));
			return;
		}

		if (isInventoryContext(perspective)) {
			applyInventoryPose(renderInfo);
			return;
		}

		if (!perspective.firstPerson()) {
			return;
		}

		float partialTick = renderInfo.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0f);
		float ads = AdsState.smoothedProgress(partialTick);
		if (ads <= 0.001f) {
			return;
		}

		GunDefinition.VisualSpec visual = renderInfo.getOrDefaultGeckolibData(GUN_VISUAL, GunDefinition.VisualSpec.DEFAULT);
		PoseStack poseStack = renderInfo.poseStack();
		boolean left = perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
		// Positive ads_offset_x: RH pulls toward screen center from a right-side hip pose.
		float side = left ? 1.0f : -1.0f;
		poseStack.translate(
				Mth.lerp(ads, 0.0f, side * visual.adsOffsetX()),
				Mth.lerp(ads, 0.0f, visual.adsOffsetY()),
				Mth.lerp(ads, 0.0f, visual.adsOffsetZ())
		);
		poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(ads, 0.0f, visual.adsPitchDegrees())));
	}

	private static boolean isInventoryContext(ItemDisplayContext perspective) {
		return perspective == ItemDisplayContext.GUI || perspective == ItemDisplayContext.ON_SHELF;
	}

	private static void applyInventoryPose(RenderPassInfo<GeoRenderState> renderInfo) {
		GunDefinition.VisualSpec visual = renderInfo.getOrDefaultGeckolibData(GUN_VISUAL, GunDefinition.VisualSpec.DEFAULT);
		PoseStack poseStack = renderInfo.poseStack();
		poseStack.mulPose(Axis.XP.rotationDegrees(visual.inventoryPitchDegrees()));
		poseStack.mulPose(Axis.YP.rotationDegrees(visual.inventoryYawDegrees()));
		float scale = visual.inventoryScale();
		poseStack.scale(scale, scale, scale);
	}
}
