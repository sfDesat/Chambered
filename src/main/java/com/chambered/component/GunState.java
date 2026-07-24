package com.chambered.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.chambered.attach.SlotType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * Per-stack firearm state. Attachment map is written by field / workbench menus.
 * Wear / jam deepen in Phase 4; {@code condition} is already consumed by the stats stack.
 */
public record GunState(
		Optional<Identifier> chamberAmmoType,
		BoltState bolt,
		FireMode fireMode,
		Optional<AmmoContents> magazine,
		Optional<Identifier> magazineItem,
		Map<SlotType, Identifier> attachments,
		float condition
) {
	private static final Codec<SlotType> SLOT_CODEC = StringRepresentable.fromEnum(SlotType::values);
	private static final Codec<Map<SlotType, Identifier>> ATTACHMENTS_CODEC =
			Codec.unboundedMap(SLOT_CODEC, Identifier.CODEC);

	private static final StreamCodec<RegistryFriendlyByteBuf, SlotType> SLOT_STREAM =
			StreamCodec.of(
					(buf, slot) -> buf.writeUtf(slot.name().toLowerCase()),
					buf -> SlotType.valueOf(buf.readUtf().toUpperCase())
			);

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<SlotType, Identifier>> ATTACHMENTS_STREAM =
			ByteBufCodecs.map(HashMap::new, SLOT_STREAM, Identifier.STREAM_CODEC);

	public static final GunState DEFAULT = new GunState(
			Optional.empty(),
			BoltState.FORWARD,
			FireMode.SEMI,
			Optional.empty(),
			Optional.empty(),
			Map.of(),
			100.0f
	);

	public GunState {
		attachments = Map.copyOf(attachments);
	}

	public static final Codec<GunState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("chamber_ammo_type").forGetter(GunState::chamberAmmoType),
			BoltState.CODEC.fieldOf("bolt").forGetter(GunState::bolt),
			FireMode.CODEC.fieldOf("fire_mode").forGetter(GunState::fireMode),
			AmmoContents.CODEC.optionalFieldOf("magazine").forGetter(GunState::magazine),
			Identifier.CODEC.optionalFieldOf("magazine_item").forGetter(GunState::magazineItem),
			ATTACHMENTS_CODEC.optionalFieldOf("attachments", Map.of()).forGetter(GunState::attachments),
			Codec.FLOAT.optionalFieldOf("condition", 100.0f).forGetter(GunState::condition)
	).apply(instance, GunState::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, GunState> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), GunState::chamberAmmoType,
			ByteBufCodecs.STRING_UTF8.map(s -> BoltState.valueOf(s.toUpperCase()), b -> b.name().toLowerCase()), GunState::bolt,
			ByteBufCodecs.STRING_UTF8.map(s -> FireMode.valueOf(s.toUpperCase()), f -> f.name().toLowerCase()), GunState::fireMode,
			AmmoContents.STREAM_CODEC.apply(ByteBufCodecs::optional), GunState::magazine,
			Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), GunState::magazineItem,
			ATTACHMENTS_STREAM, GunState::attachments,
			ByteBufCodecs.FLOAT, GunState::condition,
			GunState::new
	);

	public boolean isChambered() {
		return chamberAmmoType.isPresent();
	}

	public boolean hasMagazine() {
		return magazine.isPresent();
	}

	public GunState withChamber(Optional<Identifier> ammoType) {
		return new GunState(ammoType, bolt, fireMode, magazine, magazineItem, attachments, condition);
	}

	public GunState withChamberEmpty() {
		return withChamber(Optional.empty());
	}

	public GunState withBolt(BoltState newBolt) {
		return new GunState(chamberAmmoType, newBolt, fireMode, magazine, magazineItem, attachments, condition);
	}

	public GunState withFireMode(FireMode newMode) {
		return new GunState(chamberAmmoType, bolt, newMode, magazine, magazineItem, attachments, condition);
	}

	public GunState withMagazine(AmmoContents contents, Identifier itemId) {
		return new GunState(
				chamberAmmoType,
				bolt,
				fireMode,
				Optional.of(contents),
				Optional.of(itemId),
				attachments,
				condition
		);
	}

	/** Updates ammo payload while keeping the seated magazine item id. */
	public GunState withMagazine(AmmoContents contents) {
		return new GunState(
				chamberAmmoType,
				bolt,
				fireMode,
				Optional.of(contents),
				magazineItem,
				attachments,
				condition
		);
	}

	public GunState withMagazine(Optional<AmmoContents> contents) {
		if (contents.isEmpty()) {
			return withoutMagazine();
		}
		return new GunState(chamberAmmoType, bolt, fireMode, contents, magazineItem, attachments, condition);
	}

	public GunState withoutMagazine() {
		return new GunState(chamberAmmoType, bolt, fireMode, Optional.empty(), Optional.empty(), attachments, condition);
	}

	public GunState withAttachments(Map<SlotType, Identifier> newAttachments) {
		return new GunState(chamberAmmoType, bolt, fireMode, magazine, magazineItem, newAttachments, condition);
	}

	public GunState withCondition(float newCondition) {
		return new GunState(chamberAmmoType, bolt, fireMode, magazine, magazineItem, attachments, newCondition);
	}
}
