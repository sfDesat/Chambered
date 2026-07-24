package com.chambered.registry;

import com.chambered.ChamberedMod;
import com.chambered.entity.BulletEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	public static final EntityType<BulletEntity> BULLET = register(
			"bullet",
			EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
					.noSave()
					.sized(0.1f, 0.1f)
					.clientTrackingRange(8)
					.updateInterval(1)
	);

	private ModEntityTypes() {
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(
				Registries.ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(ChamberedMod.MOD_ID, name)
		);
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void register() {
		ChamberedMod.LOGGER.info("Registered entity types");
	}
}
