package com.chambered.client.feel;

import com.chambered.component.GunState;
import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.registry.ModComponents;
import com.chambered.stats.ResolvedGunStats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Client-predicted recoil with pattern climb + recovery, plus sway (ADS-scaled via resolved stats).
 */
public final class ClientGunFeel {
	private static final RecoilState RECOIL = new RecoilState();
	private static final SwayState SWAY = new SwayState();

	private static float pendingPitch;
	private static float pendingYaw;
	private static final float SMOOTH_FRACTION = 0.55f;

	private static float unrecoveredPitch;
	private static float unrecoveredYaw;

	private static long lastShotGameTime = Long.MIN_VALUE / 4;
	private static long swayTick;
	private static int shotsThisSession;
	private static int patternIndex;

	private ClientGunFeel() {
	}

	public static RecoilState recoil() {
		return RECOIL;
	}

	public static SwayState sway() {
		return SWAY;
	}

	public static int shotsThisSession() {
		return shotsThisSession;
	}

	public static float unrecoveredPitch() {
		return unrecoveredPitch;
	}

	/**
	 * @return true if this client tick is allowed to predict a shot (matches server RPM gate).
	 */
	public static boolean tryAcquireShot(Minecraft client, int intervalTicks) {
		if (client.level == null) {
			return false;
		}
		long now = client.level.getGameTime();
		if (now - lastShotGameTime < intervalTicks) {
			return false;
		}
		lastShotGameTime = now;
		return true;
	}

	public static void onFired(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gunItem)) {
			return;
		}

		float pitch = 1.0f;
		float yaw = 0.15f;
		var def = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(stack.getItem()));
		if (def.isPresent()) {
			ResolvedGunStats stats = ResolvedGunStats.of(def.get(), gunItem.getGunState(stack), AdsState.progress());
			pitch = stats.recoilPitch();
			yaw = stats.recoilYaw();
		}

		float patternPitch = 1.0f + Math.min(4, patternIndex) * 0.08f;
		float yawSign = (patternIndex % 2 == 0) ? 1.0f : -1.0f;
		patternIndex++;

		float pitchKick = pitch * 0.45f * patternPitch;
		float yawKick = yaw * yawSign * 0.35f;

		RECOIL.addImpulse(pitchKick, yawKick);
		pendingPitch += pitchKick;
		pendingYaw += yawKick;
		unrecoveredPitch += pitchKick;
		unrecoveredYaw += yawKick;
		shotsThisSession++;
	}

	public static void tick(Minecraft client, boolean holdingGun) {
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}

		float recovery = 0.14f;
		float swayAmount = 0.15f;
		ItemStack stack = player.getMainHandItem();
		if (holdingGun && stack.getItem() instanceof GunItem gunItem) {
			var def = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(stack.getItem()));
			if (def.isPresent()) {
				ResolvedGunStats stats = ResolvedGunStats.of(def.get(), gunItem.getGunState(stack), AdsState.progress());
				recovery = stats.recoilRecovery();
				swayAmount = stats.sway();
			}
		}

		if (Math.abs(pendingPitch) > 0.001f || Math.abs(pendingYaw) > 0.001f) {
			float applyPitch = pendingPitch * SMOOTH_FRACTION;
			float applyYaw = pendingYaw * SMOOTH_FRACTION;
			if (Math.abs(pendingPitch) < 0.05f) {
				applyPitch = pendingPitch;
			}
			if (Math.abs(pendingYaw) < 0.05f) {
				applyYaw = pendingYaw;
			}
			player.setXRot(player.getXRot() - applyPitch);
			player.setYRot(player.getYRot() + applyYaw);
			pendingPitch -= applyPitch;
			pendingYaw -= applyYaw;
		}

		if (Math.abs(unrecoveredPitch) > 0.001f || Math.abs(unrecoveredYaw) > 0.001f) {
			float ads = AdsState.progress();
			float rate = recovery * Mth.lerp(ads, 0.85f, 1.25f);
			float recPitch = unrecoveredPitch * rate;
			float recYaw = unrecoveredYaw * rate;
			player.setXRot(player.getXRot() + recPitch);
			player.setYRot(player.getYRot() - recYaw);
			unrecoveredPitch -= recPitch;
			unrecoveredYaw -= recYaw;
		}

		RECOIL.tick(1.0f - recovery);

		if (!holdingGun) {
			SWAY.set(0.0f, 0.0f);
			patternIndex = 0;
			unrecoveredPitch = 0.0f;
			unrecoveredYaw = 0.0f;
			return;
		}

		swayTick++;
		float ads = AdsState.progress();
		float pitchSway = Mth.sin(swayTick * 0.07f) * swayAmount * 0.15f;
		float yawSway = Mth.cos(swayTick * 0.05f) * swayAmount * 0.12f;
		SWAY.set(pitchSway, yawSway);

		if (ads > 0.2f) {
			player.setXRot(player.getXRot() + pitchSway * 0.15f);
			player.setYRot(player.getYRot() + yawSway * 0.15f);
		}
	}

	public static GunState currentGunState(Minecraft client) {
		if (client.player == null) {
			return GunState.DEFAULT;
		}
		ItemStack stack = client.player.getMainHandItem();
		return stack.getOrDefault(ModComponents.GUN_STATE, GunState.DEFAULT);
	}
}
