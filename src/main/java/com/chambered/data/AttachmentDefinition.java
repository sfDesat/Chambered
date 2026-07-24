package com.chambered.data;

import java.util.List;

import com.chambered.attach.SlotType;
import com.chambered.stats.StatModifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * Data-driven attachment. Definition id matches the attachment item registry id.
 */
public record AttachmentDefinition(
		String displayName,
		SlotType slot,
		boolean fieldAttachable,
		List<Identifier> compatibleGuns,
		StatModifier modifiers
) {
	private static final Codec<SlotType> SLOT_CODEC = StringRepresentable.fromEnum(SlotType::values);

	public static final Codec<AttachmentDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("display_name").forGetter(AttachmentDefinition::displayName),
			SLOT_CODEC.fieldOf("slot").forGetter(AttachmentDefinition::slot),
			Codec.BOOL.optionalFieldOf("field_attachable", true).forGetter(AttachmentDefinition::fieldAttachable),
			Identifier.CODEC.listOf().optionalFieldOf("compatible_guns", List.of()).forGetter(AttachmentDefinition::compatibleGuns),
			StatModifier.CODEC.optionalFieldOf("modifiers", StatModifier.IDENTITY).forGetter(AttachmentDefinition::modifiers)
	).apply(instance, AttachmentDefinition::new));

	/**
	 * Empty {@code compatible_guns} means any gun that exposes this slot.
	 */
	public boolean fitsGun(Identifier gunId) {
		return compatibleGuns.isEmpty() || compatibleGuns.contains(gunId);
	}
}
