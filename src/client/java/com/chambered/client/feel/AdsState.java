package com.chambered.client.feel;

import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.stats.ResolvedGunStats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Client ADS progress driven by resolved ergonomics / aim times.
 */
public final class AdsState {
	private static boolean wantingAds;
	private static float progress;
	private static float previousProgress;
	private static float fovMultiplier = 1.0f;

	private AdsState() {
	}

	public static void setWantingAds(boolean wanting) {
		wantingAds = wanting;
	}

	public static boolean wantingAds() {
		return wantingAds;
	}

	/** 0 = hip, 1 = fully aimed (last tick). */
	public static float progress() {
		return progress;
	}

	/** Frame-smoothed ADS amount for rendering. */
	public static float smoothedProgress(float partialTick) {
		return Mth.lerp(partialTick, previousProgress, progress);
	}

	public static boolean isAiming() {
		return progress >= 0.85f;
	}

	public static float fovMultiplier() {
		return fovMultiplier;
	}

	public static void reset() {
		wantingAds = false;
		previousProgress = 0.0f;
		progress = 0.0f;
		fovMultiplier = 1.0f;
	}

	public static void tick(Minecraft client, boolean holdingGun) {
		if (!holdingGun) {
			reset();
			return;
		}

		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}

		ItemStack stack = player.getMainHandItem();
		float enter = 0.25f;
		float exit = 0.2f;
		float scale = 1.0f;
		float adsFov = 0.85f;
		if (stack.getItem() instanceof GunItem gunItem) {
			var def = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(stack.getItem()));
			if (def.isPresent()) {
				ResolvedGunStats stats = ResolvedGunStats.of(def.get(), gunItem.getGunState(stack), progress);
				enter = stats.adsEnterTime();
				exit = stats.adsExitTime();
				scale = stats.adsTimeScale();
				adsFov = stats.adsFovMultiplier();
			}
		}

		previousProgress = progress;
		float dt = 1.0f / 20.0f;
		if (wantingAds) {
			progress = Math.min(1.0f, progress + dt / (enter * scale));
		} else {
			progress = Math.max(0.0f, progress - dt / (exit * scale));
		}
		fovMultiplier = Mth.lerp(progress, 1.0f, adsFov);
	}
}
