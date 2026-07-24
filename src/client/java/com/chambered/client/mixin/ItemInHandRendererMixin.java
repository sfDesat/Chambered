package com.chambered.client.mixin;

import com.chambered.item.GunItem;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Softens vanilla held-item view-bob sway while holding a Chambered gun.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	/** Fraction of vanilla hand bob retained while holding a gun. */
	@Unique
	private static final float GUN_BOB_SCALE = 0.28f;

	@Unique
	private boolean chambered$holdingGun;

	@Inject(method = "submitHandsWithItems", at = @At("HEAD"))
	private void chambered$trackGun(
			float frameInterp,
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			LocalPlayer player,
			int lightCoords,
			CallbackInfo ci
	) {
		ItemStack main = player.getMainHandItem();
		this.chambered$holdingGun = main.getItem() instanceof GunItem;
	}

	@ModifyConstant(method = "submitHandsWithItems", constant = @Constant(floatValue = 0.1F))
	private float chambered$reduceGunBob(float bobFactor) {
		return this.chambered$holdingGun ? bobFactor * GUN_BOB_SCALE : bobFactor;
	}
}
