package com.chambered.stats;

import com.chambered.data.GunDefinition;

import net.minecraft.util.Mth;

/**
 * Final handling / ballistics numbers after base definition + {@link ModifierStack} + ADS context.
 * Fire, feel, and ADS code should read this — not raw {@link GunDefinition} fields.
 */
public record ResolvedGunStats(
		float recoilPitch,
		float recoilYaw,
		float sway,
		float ergonomics,
		float adsEnterTime,
		float adsExitTime,
		/** Weapon mass in kilograms (empty / nominal; attachments add via modifiers). */
		float weightKg,
		float hipfireSpread,
		float adsSpread,
		/** Spread to use for the current ADS amount. */
		float effectiveSpread,
		float drawTime,
		float holsterTime,
		float recoilRecovery,
		float adsFovMultiplier,
		float barrelVelocityFactor,
		float adsTimeScale,
		int reloadTicks,
		int fireIntervalTicks,
		float adsProgress
) {
	/**
	 * @param adsProgress 0 = hip, 1 = fully aimed
	 */
	public static ResolvedGunStats resolve(GunDefinition def, ModifierStack stack, float adsProgress) {
		StatModifier m = stack.combined();
		GunDefinition.HandlingSpec h = def.handling();
		float ads = Mth.clamp(adsProgress, 0.0f, 1.0f);

		float ergonomics = Math.max(1.0f, h.ergonomics() + m.ergonomicsAdd());
		float adsTimeScale = 100.0f / (50.0f + ergonomics);

		float recoilPitch = (h.recoilPitch() * m.recoilPitchMul() + m.recoilPitchAdd())
				* Mth.lerp(ads, 1.0f, 0.65f);
		float recoilYaw = (h.recoilYaw() * m.recoilYawMul() + m.recoilYawAdd())
				* Mth.lerp(ads, 1.0f, 0.65f);
		float sway = (h.sway() * m.swayMul() + m.swayAdd())
				* Mth.lerp(ads, 1.0f, 0.35f);

		float hipSpread = h.hipfireSpread() * m.hipfireSpreadMul();
		float adsSpread = h.adsSpread() * m.adsSpreadMul();
		float effectiveSpread = Mth.lerp(ads, hipSpread, adsSpread);

		float enter = Math.max(0.05f, h.adsEnterTime() * m.adsEnterTimeMul());
		float exit = Math.max(0.05f, h.adsExitTime() * m.adsExitTimeMul());
		float weightKg = Math.max(0.0f, h.weightKg() + m.weightAdd());
		float draw = Math.max(0.05f, h.drawTime() * m.drawTimeMul());
		float holster = Math.max(0.05f, h.holsterTime() * m.holsterTimeMul());
		float recovery = Math.max(0.01f, h.recoilRecovery() * m.recoilRecoveryMul());
		float velocity = def.ballistics().barrelVelocityFactor() * m.barrelVelocityMul();

		int reloadTicks = Math.max(1, Math.round(h.reloadTime() * 20.0f));
		int fireInterval = Math.max(1, Math.round(1200.0f / Math.max(1, def.fireControl().rpm())));

		return new ResolvedGunStats(
				recoilPitch,
				recoilYaw,
				sway,
				ergonomics,
				enter,
				exit,
				weightKg,
				hipSpread,
				adsSpread,
				effectiveSpread,
				draw,
				holster,
				recovery,
				h.adsFovMultiplier(),
				velocity,
				adsTimeScale,
				reloadTicks,
				fireInterval,
				ads
		);
	}

	/** Convenience: resolve from def + gun state + ADS. */
	public static ResolvedGunStats of(GunDefinition def, com.chambered.component.GunState state, float adsProgress) {
		return resolve(def, ModifierStack.fromGunState(state), adsProgress);
	}
}
