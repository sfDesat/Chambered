package com.chambered.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Cartridge family / mechanical compatibility.
 */
public record CaliberDefinition(
		String displayName,
		float baseVelocity,
		float gravity,
		float drag
) {
	public static final Codec<CaliberDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("display_name").forGetter(CaliberDefinition::displayName),
			Codec.FLOAT.fieldOf("base_velocity").forGetter(CaliberDefinition::baseVelocity),
			Codec.FLOAT.optionalFieldOf("gravity", 0.03f).forGetter(CaliberDefinition::gravity),
			Codec.FLOAT.optionalFieldOf("drag", 0.01f).forGetter(CaliberDefinition::drag)
	).apply(instance, CaliberDefinition::new));

	/**
	 * Minecraft air inertia multiplier (1 = no drag).
	 */
	public float airDragFactor() {
		return Math.max(0.0f, 1.0f - drag);
	}
}
