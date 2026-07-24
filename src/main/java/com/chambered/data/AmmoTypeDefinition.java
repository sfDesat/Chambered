package com.chambered.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * Load within a caliber (FMJ, HP, AP…). Phase 1 ships one type.
 */
public record AmmoTypeDefinition(
		Identifier caliber,
		String displayName,
		float damage,
		float velocityMultiplier,
		float dragMultiplier
) {
	public static final Codec<AmmoTypeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("caliber").forGetter(AmmoTypeDefinition::caliber),
			Codec.STRING.fieldOf("display_name").forGetter(AmmoTypeDefinition::displayName),
			Codec.FLOAT.fieldOf("damage").forGetter(AmmoTypeDefinition::damage),
			Codec.FLOAT.optionalFieldOf("velocity_multiplier", 1.0f).forGetter(AmmoTypeDefinition::velocityMultiplier),
			Codec.FLOAT.optionalFieldOf("drag_multiplier", 1.0f).forGetter(AmmoTypeDefinition::dragMultiplier)
	).apply(instance, AmmoTypeDefinition::new));
}
