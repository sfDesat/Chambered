package com.chambered.data;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * Magazine capacity and caliber. Runtime mag→gun checks use the gun's
 * {@code accepted_magazines} list only; {@code compatible_guns} is optional metadata
 * for future tooling / datapack validation.
 */
public record MagazineDefinition(
		String displayName,
		int capacity,
		Identifier caliber,
		Identifier defaultAmmoType,
		List<Identifier> compatibleGuns
) {
	public static final Codec<MagazineDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("display_name").forGetter(MagazineDefinition::displayName),
			Codec.INT.fieldOf("capacity").forGetter(MagazineDefinition::capacity),
			Identifier.CODEC.fieldOf("caliber").forGetter(MagazineDefinition::caliber),
			Identifier.CODEC.fieldOf("default_ammo_type").forGetter(MagazineDefinition::defaultAmmoType),
			Identifier.CODEC.listOf().optionalFieldOf("compatible_guns", List.of()).forGetter(MagazineDefinition::compatibleGuns)
	).apply(instance, MagazineDefinition::new));
}
