package com.chambered.action;

import java.util.Optional;

import com.chambered.ChamberedMod;
import com.chambered.anim.GunAnimations;
import com.chambered.ballistics.Ballistics;
import com.chambered.component.AmmoContents;
import com.chambered.component.BoltState;
import com.chambered.component.GunState;
import com.chambered.data.AmmoTypeDefinition;
import com.chambered.data.CaliberDefinition;
import com.chambered.data.GunDefinition;
import com.chambered.data.GunDefinitions;
import com.chambered.entity.BulletEntity;
import com.chambered.item.GunItem;
import com.chambered.stats.ResolvedGunStats;

import com.geckolib.animatable.GeoItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative fire: validate gun/bolt/chamber/mode/cooldown, spawn bullet, cycle action.
 */
public final class FireHandler {
	private FireHandler() {
	}

	public static void handle(ServerPlayer player, boolean aiming) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gunItem)) {
			return;
		}

		Identifier gunId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		Optional<GunDefinition> gunDefOpt = GunDefinitions.gun(gunId);
		if (gunDefOpt.isEmpty()) {
			ChamberedMod.LOGGER.warn("No gun definition for {}", gunId);
			return;
		}
		GunDefinition gunDef = gunDefOpt.get();
		GunState state = gunItem.getGunState(stack);
		ResolvedGunStats stats = ResolvedGunStats.of(gunDef, state, aiming ? 1.0f : 0.0f);

		if (ActionTimeline.isBusy(player)) {
			return;
		}
		if (!EquipGates.canFire(player)) {
			return;
		}
		if (!gunDef.allowsFireMode(state.fireMode())) {
			return;
		}
		if (!GunCooldowns.tryAcquire(player, stats.fireIntervalTicks())) {
			return;
		}
		ServerLevel level = player.level();
		if (state.bolt() == BoltState.LOCKED_BACK) {
			GunSounds.playEmpty(player, gunDef);
			gunItem.triggerAnim(player, GeoItem.getOrAssignId(stack, level), GunAnimations.CONTROLLER, "empty");
			return;
		}
		if (!state.isChambered()) {
			GunSounds.playEmpty(player, gunDef);
			gunItem.triggerAnim(player, GeoItem.getOrAssignId(stack, level), GunAnimations.CONTROLLER, "empty");
			return;
		}

		Identifier ammoTypeId = state.chamberAmmoType().orElseThrow();
		Optional<AmmoTypeDefinition> ammoOpt = GunDefinitions.ammoType(ammoTypeId);
		Optional<CaliberDefinition> caliberOpt = GunDefinitions.caliber(gunDef.caliber());
		if (ammoOpt.isEmpty() || caliberOpt.isEmpty()) {
			ChamberedMod.LOGGER.warn("Missing ammo/caliber defs for fire on {}", gunId);
			return;
		}

		AmmoTypeDefinition ammo = ammoOpt.get();
		CaliberDefinition caliber = caliberOpt.get();
		if (!ammo.caliber().equals(gunDef.caliber())) {
			ChamberedMod.LOGGER.warn("Ammo {} caliber mismatch for gun {}", ammoTypeId, gunId);
			return;
		}

		spawnBullet(player, level, stack, gunDef, stats, caliber, ammo);
		GunSounds.playFire(player, gunDef);
		gunItem.setGunState(stack, cycleAction(state));
		gunItem.triggerAnim(player, GeoItem.getOrAssignId(stack, level), GunAnimations.CONTROLLER, "fire");
	}

	private static void spawnBullet(
			ServerPlayer player,
			ServerLevel level,
			ItemStack gunStack,
			GunDefinition gunDef,
			ResolvedGunStats stats,
			CaliberDefinition caliber,
			AmmoTypeDefinition ammo
	) {
		float speed = Ballistics.muzzleVelocity(stats.barrelVelocityFactor(), caliber, ammo);
		float gravity = Ballistics.gravity(caliber);
		float airDrag = Ballistics.airDrag(caliber, ammo);
		float damage = Ballistics.damage(ammo);

		BulletEntity bullet = new BulletEntity(level);
		bullet.setOwner(player);
		Vec3 eye = player.getEyePosition();
		bullet.setPos(eye.x, eye.y - 0.05, eye.z);
		bullet.configure(damage, gravity, airDrag, gunDef.maxBulletLifetimeTicks(), gunDef.maxBulletRange());
		Projectile.spawnProjectile(bullet, level, gunStack, b ->
				b.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, speed, stats.effectiveSpread())
		);
	}

	/**
	 * Consume chamber, feed from mag if possible; empty mag locks the slide.
	 */
	private static GunState cycleAction(GunState state) {
		Optional<AmmoContents> magOpt = state.magazine();
		if (magOpt.isPresent() && !magOpt.get().isEmpty()) {
			AmmoContents mag = magOpt.get();
			Identifier nextAmmo = mag.ammoType();
			AmmoContents depleted = mag.withCount(mag.count() - 1);
			return state
					.withChamber(Optional.of(nextAmmo))
					.withBolt(BoltState.FORWARD)
					.withMagazine(depleted);
		}
		return state
				.withChamberEmpty()
				.withBolt(BoltState.LOCKED_BACK)
				.withMagazine(magOpt);
	}
}
