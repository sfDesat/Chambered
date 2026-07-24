package com.chambered.ballistics;

import com.chambered.data.AmmoTypeDefinition;
import com.chambered.data.CaliberDefinition;

/**
 * Ballistics helpers for muzzle velocity, drag, and damage.
 * Barrel velocity factor comes from {@link com.chambered.stats.ResolvedGunStats}.
 */
public final class Ballistics {
	private Ballistics() {
	}

	public static float muzzleVelocity(float barrelVelocityFactor, CaliberDefinition caliber, AmmoTypeDefinition ammo) {
		return caliber.baseVelocity() * barrelVelocityFactor * ammo.velocityMultiplier();
	}

	public static float gravity(CaliberDefinition caliber) {
		return caliber.gravity();
	}

	public static float airDrag(CaliberDefinition caliber, AmmoTypeDefinition ammo) {
		float drag = caliber.drag() * ammo.dragMultiplier();
		return Math.max(0.0f, 1.0f - drag);
	}

	public static float damage(AmmoTypeDefinition ammo) {
		return ammo.damage();
	}
}
