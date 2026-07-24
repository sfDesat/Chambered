package com.chambered.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Additive / multiplicative deltas applied on top of a gun's base definition stats.
 * Attachments, condition, and ADS context feed these into {@link ModifierStack}.
 */
public record StatModifier(
		float recoilPitchMul,
		float recoilPitchAdd,
		float recoilYawMul,
		float recoilYawAdd,
		float swayMul,
		float swayAdd,
		float ergonomicsAdd,
		float adsEnterTimeMul,
		float adsExitTimeMul,
		float weightAdd,
		float hipfireSpreadMul,
		float adsSpreadMul,
		float recoilRecoveryMul,
		float barrelVelocityMul,
		float drawTimeMul,
		float holsterTimeMul
) {
	public static final StatModifier IDENTITY = new StatModifier(
			1.0f, 0.0f,
			1.0f, 0.0f,
			1.0f, 0.0f,
			0.0f,
			1.0f, 1.0f,
			0.0f,
			1.0f, 1.0f,
			1.0f,
			1.0f,
			1.0f, 1.0f
	);

	public static final Codec<StatModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("recoil_pitch_mul", 1.0f).forGetter(StatModifier::recoilPitchMul),
			Codec.FLOAT.optionalFieldOf("recoil_pitch_add", 0.0f).forGetter(StatModifier::recoilPitchAdd),
			Codec.FLOAT.optionalFieldOf("recoil_yaw_mul", 1.0f).forGetter(StatModifier::recoilYawMul),
			Codec.FLOAT.optionalFieldOf("recoil_yaw_add", 0.0f).forGetter(StatModifier::recoilYawAdd),
			Codec.FLOAT.optionalFieldOf("sway_mul", 1.0f).forGetter(StatModifier::swayMul),
			Codec.FLOAT.optionalFieldOf("sway_add", 0.0f).forGetter(StatModifier::swayAdd),
			Codec.FLOAT.optionalFieldOf("ergonomics_add", 0.0f).forGetter(StatModifier::ergonomicsAdd),
			Codec.FLOAT.optionalFieldOf("ads_enter_time_mul", 1.0f).forGetter(StatModifier::adsEnterTimeMul),
			Codec.FLOAT.optionalFieldOf("ads_exit_time_mul", 1.0f).forGetter(StatModifier::adsExitTimeMul),
			Codec.FLOAT.optionalFieldOf("weight_add", 0.0f).forGetter(StatModifier::weightAdd),
			Codec.FLOAT.optionalFieldOf("hipfire_spread_mul", 1.0f).forGetter(StatModifier::hipfireSpreadMul),
			Codec.FLOAT.optionalFieldOf("ads_spread_mul", 1.0f).forGetter(StatModifier::adsSpreadMul),
			Codec.FLOAT.optionalFieldOf("recoil_recovery_mul", 1.0f).forGetter(StatModifier::recoilRecoveryMul),
			Codec.FLOAT.optionalFieldOf("barrel_velocity_mul", 1.0f).forGetter(StatModifier::barrelVelocityMul),
			Codec.FLOAT.optionalFieldOf("draw_time_mul", 1.0f).forGetter(StatModifier::drawTimeMul),
			Codec.FLOAT.optionalFieldOf("holster_time_mul", 1.0f).forGetter(StatModifier::holsterTimeMul)
	).apply(instance, StatModifier::new));

	public StatModifier combine(StatModifier other) {
		return new StatModifier(
				recoilPitchMul * other.recoilPitchMul,
				recoilPitchAdd + other.recoilPitchAdd,
				recoilYawMul * other.recoilYawMul,
				recoilYawAdd + other.recoilYawAdd,
				swayMul * other.swayMul,
				swayAdd + other.swayAdd,
				ergonomicsAdd + other.ergonomicsAdd,
				adsEnterTimeMul * other.adsEnterTimeMul,
				adsExitTimeMul * other.adsExitTimeMul,
				weightAdd + other.weightAdd,
				hipfireSpreadMul * other.hipfireSpreadMul,
				adsSpreadMul * other.adsSpreadMul,
				recoilRecoveryMul * other.recoilRecoveryMul,
				barrelVelocityMul * other.barrelVelocityMul,
				drawTimeMul * other.drawTimeMul,
				holsterTimeMul * other.holsterTimeMul
		);
	}
}
