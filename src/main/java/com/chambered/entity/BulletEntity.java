package com.chambered.entity;

import java.util.concurrent.atomic.AtomicInteger;

import com.chambered.registry.ModEntityTypes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Performance-minded ballistic projectile: gravity, drag, lifetime/range caps, cheap tick.
 */
public class BulletEntity extends ThrowableProjectile {
	/** Absolute safety cap if a def sets a huge lifetime. */
	public static final int HARD_MAX_LIFE_TICKS = 100;

	private static final AtomicInteger ACTIVE = new AtomicInteger();

	private float damage = 1.0f;
	private float gravity = 0.03f;
	private float airDrag = 0.99f;
	private int maxLifeTicks = 80;
	private float maxRange = 128.0f;
	private Vec3 startPos = Vec3.ZERO;
	private boolean counted;

	public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
		super(type, level);
	}

	public BulletEntity(Level level) {
		this(ModEntityTypes.BULLET, level);
	}

	public static int activeCount() {
		return ACTIVE.get();
	}

	public void configure(float damage, float gravity, float airDrag, int maxLifeTicks, float maxRange) {
		this.damage = damage;
		this.gravity = gravity;
		this.airDrag = airDrag;
		this.maxLifeTicks = Math.min(Math.max(1, maxLifeTicks), HARD_MAX_LIFE_TICKS);
		this.maxRange = maxRange;
		this.startPos = this.position();
		if (!this.counted) {
			ACTIVE.incrementAndGet();
			this.counted = true;
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		if (this.counted) {
			ACTIVE.decrementAndGet();
			this.counted = false;
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected double getDefaultGravity() {
		return this.gravity;
	}

	@Override
	protected float getAirDrag() {
		return this.airDrag;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			return;
		}
		if (this.tickCount > this.maxLifeTicks || this.tickCount > HARD_MAX_LIFE_TICKS) {
			this.discard();
			return;
		}
		if (this.startPos != Vec3.ZERO && this.startPos.distanceToSqr(this.position()) > this.maxRange * this.maxRange) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide() && result.getType() != HitResult.Type.MISS) {
			this.spawnHitParticles(result.getLocation());
			this.discard();
		}
	}

	private void spawnHitParticles(Vec3 pos) {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		// Subtle puff — one effect for now; expand later per surface/material.
		serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 3, 0.04, 0.04, 0.04, 0.01);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		Entity target = hitResult.getEntity();
		Entity owner = this.getOwner();
		DamageSource damageSource = this.damageSources().thrown(this, owner);
		if (this.level() instanceof ServerLevel serverLevel && target.hurtServer(serverLevel, damageSource, this.damage)) {
			if (owner instanceof LivingEntity livingOwner) {
				EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
				livingOwner.setLastHurtMob(target);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		if (!this.level().isClientSide()) {
			this.discard();
		}
	}

	public int maxLifeTicks() {
		return this.maxLifeTicks;
	}

	public float maxRange() {
		return this.maxRange;
	}

	public int ageTicks() {
		return this.tickCount;
	}
}
