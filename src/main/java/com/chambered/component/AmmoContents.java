package com.chambered.component;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * Magazine ammo payload. Caliber and ammo type ids point at definition data (Phase 1).
 */
public record AmmoContents(
		int count,
		int capacity,
		Identifier caliber,
		Identifier ammoType
) {
	public static final Codec<AmmoContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("count").forGetter(AmmoContents::count),
			Codec.INT.fieldOf("capacity").forGetter(AmmoContents::capacity),
			Identifier.CODEC.fieldOf("caliber").forGetter(AmmoContents::caliber),
			Identifier.CODEC.fieldOf("ammo_type").forGetter(AmmoContents::ammoType)
	).apply(instance, AmmoContents::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AmmoContents> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, AmmoContents::count,
			ByteBufCodecs.VAR_INT, AmmoContents::capacity,
			Identifier.STREAM_CODEC, AmmoContents::caliber,
			Identifier.STREAM_CODEC, AmmoContents::ammoType,
			AmmoContents::new
	);

	public AmmoContents {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity must be >= 0");
		}
		if (count < 0 || count > capacity) {
			throw new IllegalArgumentException("count must be between 0 and capacity");
		}
	}

	public boolean isEmpty() {
		return count <= 0;
	}

	public AmmoContents withCount(int newCount) {
		return new AmmoContents(newCount, capacity, caliber, ammoType);
	}

	public static Optional<AmmoContents> optionalOf(AmmoContents contents) {
		return Optional.ofNullable(contents);
	}
}
