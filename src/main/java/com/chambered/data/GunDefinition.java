package com.chambered.data;

import java.util.List;

import com.chambered.attach.SlotType;
import com.chambered.component.FireMode;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * Data-driven firearm stats. Reliability fields (jam) reserved for Phase 4;
 * attachment slot list reserved for Phase 3. Combat code should prefer
 * {@link com.chambered.stats.ResolvedGunStats} over reading these raw.
 */
public record GunDefinition(
		String displayName,
		Identifier caliber,
		List<Identifier> acceptedMagazines,
		BallisticsSpec ballistics,
		FireControlSpec fireControl,
		HandlingSpec handling,
		SoundSpec sounds,
		VisualSpec visual,
		List<SlotType> slots,
		float jamBase
) {
	private static final Codec<SlotType> SLOT_CODEC = StringRepresentable.fromEnum(SlotType::values);

	public static final Codec<GunDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("display_name").forGetter(GunDefinition::displayName),
			Identifier.CODEC.fieldOf("caliber").forGetter(GunDefinition::caliber),
			Identifier.CODEC.listOf().fieldOf("accepted_magazines").forGetter(GunDefinition::acceptedMagazines),
			BallisticsSpec.CODEC.fieldOf("ballistics").forGetter(GunDefinition::ballistics),
			FireControlSpec.CODEC.fieldOf("fire_control").forGetter(GunDefinition::fireControl),
			HandlingSpec.CODEC.fieldOf("handling").forGetter(GunDefinition::handling),
			SoundSpec.CODEC.optionalFieldOf("sounds", SoundSpec.DEFAULT).forGetter(GunDefinition::sounds),
			VisualSpec.CODEC.optionalFieldOf("visual", VisualSpec.DEFAULT).forGetter(GunDefinition::visual),
			SLOT_CODEC.listOf().optionalFieldOf("slots", List.of()).forGetter(GunDefinition::slots),
			Codec.FLOAT.optionalFieldOf("jam_base", 0.0f).forGetter(GunDefinition::jamBase)
	).apply(instance, GunDefinition::new));

	public record BallisticsSpec(
			float barrelVelocityFactor,
			int pelletCount,
			int maxBulletLifetimeTicks,
			float maxBulletRange
	) {
		public static final Codec<BallisticsSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("barrel_velocity_factor", 1.0f).forGetter(BallisticsSpec::barrelVelocityFactor),
				Codec.INT.optionalFieldOf("pellet_count", 1).forGetter(BallisticsSpec::pelletCount),
				Codec.INT.optionalFieldOf("max_bullet_lifetime_ticks", 80).forGetter(BallisticsSpec::maxBulletLifetimeTicks),
				Codec.FLOAT.optionalFieldOf("max_bullet_range", 128.0f).forGetter(BallisticsSpec::maxBulletRange)
		).apply(instance, BallisticsSpec::new));
	}

	public record FireControlSpec(
			List<FireMode> fireModes,
			int rpm,
			int cycleTimeTicks
	) {
		public static final Codec<FireControlSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				FireMode.CODEC.listOf().optionalFieldOf("fire_modes", List.of(FireMode.SEMI)).forGetter(FireControlSpec::fireModes),
				Codec.INT.optionalFieldOf("rpm", 400).forGetter(FireControlSpec::rpm),
				Codec.INT.optionalFieldOf("cycle_time_ticks", 2).forGetter(FireControlSpec::cycleTimeTicks)
		).apply(instance, FireControlSpec::new));
	}

	/**
	 * Base handling. {@code weight} is kilograms. {@code reload_time} is seconds (action timeline).
	 * {@code holster_time} / {@code weight} feed future systems (stamina, holster anim).
	 */
	public record HandlingSpec(
			float recoilPitch,
			float recoilYaw,
			float sway,
			float ergonomics,
			float adsEnterTime,
			float adsExitTime,
			float weightKg,
			float hipfireSpread,
			float adsSpread,
			float drawTime,
			float holsterTime,
			float recoilRecovery,
			float adsFovMultiplier,
			float reloadTime
	) {
		public static final Codec<HandlingSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("recoil_pitch", 2.5f).forGetter(HandlingSpec::recoilPitch),
				Codec.FLOAT.optionalFieldOf("recoil_yaw", 0.4f).forGetter(HandlingSpec::recoilYaw),
				Codec.FLOAT.optionalFieldOf("sway", 0.15f).forGetter(HandlingSpec::sway),
				Codec.FLOAT.optionalFieldOf("ergonomics", 70.0f).forGetter(HandlingSpec::ergonomics),
				Codec.FLOAT.optionalFieldOf("ads_enter_time", 0.25f).forGetter(HandlingSpec::adsEnterTime),
				Codec.FLOAT.optionalFieldOf("ads_exit_time", 0.2f).forGetter(HandlingSpec::adsExitTime),
				Codec.FLOAT.optionalFieldOf("weight", 1.0f).forGetter(HandlingSpec::weightKg),
				Codec.FLOAT.optionalFieldOf("hipfire_spread", 2.5f).forGetter(HandlingSpec::hipfireSpread),
				Codec.FLOAT.optionalFieldOf("ads_spread", 0.35f).forGetter(HandlingSpec::adsSpread),
				Codec.FLOAT.optionalFieldOf("draw_time", 0.35f).forGetter(HandlingSpec::drawTime),
				Codec.FLOAT.optionalFieldOf("holster_time", 0.25f).forGetter(HandlingSpec::holsterTime),
				Codec.FLOAT.optionalFieldOf("recoil_recovery", 0.14f).forGetter(HandlingSpec::recoilRecovery),
				Codec.FLOAT.optionalFieldOf("ads_fov_multiplier", 0.85f).forGetter(HandlingSpec::adsFovMultiplier),
				Codec.FLOAT.optionalFieldOf("reload_time", 1.0f).forGetter(HandlingSpec::reloadTime)
		).apply(instance, HandlingSpec::new));
	}

	/** Sound event ids played by handlers. Defaults match prior hardcoded vanilla stubs. */
	public record SoundSpec(
			Identifier fire,
			Identifier empty,
			Identifier reload,
			Identifier fireMode
	) {
		public static final SoundSpec DEFAULT = new SoundSpec(
				Identifier.withDefaultNamespace("entity.firework_rocket.blast"),
				Identifier.withDefaultNamespace("ui.button.click"),
				Identifier.withDefaultNamespace("block.iron_door.close"),
				Identifier.withDefaultNamespace("ui.button.click")
		);

		public static final Codec<SoundSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.optionalFieldOf("fire", DEFAULT.fire).forGetter(SoundSpec::fire),
				Identifier.CODEC.optionalFieldOf("empty", DEFAULT.empty).forGetter(SoundSpec::empty),
				Identifier.CODEC.optionalFieldOf("reload", DEFAULT.reload).forGetter(SoundSpec::reload),
				Identifier.CODEC.optionalFieldOf("fire_mode", DEFAULT.fireMode).forGetter(SoundSpec::fireMode)
		).apply(instance, SoundSpec::new));
	}

	/** First-person pose, inventory icon, and slide lock presentation (per-gun). */
	public record VisualSpec(
			float slideLockedZ,
			float adsOffsetX,
			float adsOffsetY,
			float adsOffsetZ,
			float adsPitchDegrees,
			float inventoryScale,
			float inventoryPitchDegrees,
			float inventoryYawDegrees
	) {
		public static final VisualSpec DEFAULT = new VisualSpec(
				1.45f, 0.38f, 0.135f, 0.20f, -4.8f,
				1.0f, 20.0f, 80.0f
		);

		public static final Codec<VisualSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("slide_locked_z", DEFAULT.slideLockedZ).forGetter(VisualSpec::slideLockedZ),
				Codec.FLOAT.optionalFieldOf("ads_offset_x", DEFAULT.adsOffsetX).forGetter(VisualSpec::adsOffsetX),
				Codec.FLOAT.optionalFieldOf("ads_offset_y", DEFAULT.adsOffsetY).forGetter(VisualSpec::adsOffsetY),
				Codec.FLOAT.optionalFieldOf("ads_offset_z", DEFAULT.adsOffsetZ).forGetter(VisualSpec::adsOffsetZ),
				Codec.FLOAT.optionalFieldOf("ads_pitch_degrees", DEFAULT.adsPitchDegrees).forGetter(VisualSpec::adsPitchDegrees),
				Codec.FLOAT.optionalFieldOf("inventory_scale", DEFAULT.inventoryScale).forGetter(VisualSpec::inventoryScale),
				Codec.FLOAT.optionalFieldOf("inventory_pitch_degrees", DEFAULT.inventoryPitchDegrees)
						.forGetter(VisualSpec::inventoryPitchDegrees),
				Codec.FLOAT.optionalFieldOf("inventory_yaw_degrees", DEFAULT.inventoryYawDegrees)
						.forGetter(VisualSpec::inventoryYawDegrees)
		).apply(instance, VisualSpec::new));
	}

	public boolean acceptsMagazine(Identifier magazineId) {
		return acceptedMagazines.contains(magazineId);
	}

	public boolean allowsFireMode(FireMode mode) {
		return fireControl.fireModes().contains(mode);
	}

	public FireMode defaultFireMode() {
		List<FireMode> modes = fireControl.fireModes();
		return modes.isEmpty() ? FireMode.SEMI : modes.getFirst();
	}

	public int fireIntervalTicks() {
		return Math.max(1, Math.round(1200.0f / Math.max(1, fireControl.rpm())));
	}

	public int reloadTicks() {
		return Math.max(1, Math.round(handling.reloadTime() * 20.0f));
	}

	public float barrelVelocityFactor() {
		return ballistics.barrelVelocityFactor();
	}

	public int maxBulletLifetimeTicks() {
		return ballistics.maxBulletLifetimeTicks();
	}

	public float maxBulletRange() {
		return ballistics.maxBulletRange();
	}

	public List<FireMode> fireModes() {
		return fireControl.fireModes();
	}
}
