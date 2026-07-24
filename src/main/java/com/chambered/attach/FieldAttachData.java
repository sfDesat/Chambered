package com.chambered.attach;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

/**
 * Opening data for the field attach menu (synced via {@link net.fabricmc.fabric.api.menu.v1.ExtendedMenuType}).
 */
public record FieldAttachData(InteractionHand hand, List<SlotType> slots) {
	public static final StreamCodec<RegistryFriendlyByteBuf, FieldAttachData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.map(
					s -> InteractionHand.valueOf(s.toUpperCase()),
					h -> h.name().toLowerCase()
			),
			FieldAttachData::hand,
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(
					list -> {
						List<SlotType> parsed = new ArrayList<>(list.size());
						for (String name : list) {
							parsed.add(SlotType.valueOf(name.toUpperCase()));
						}
						return List.copyOf(parsed);
					},
					slots -> {
						List<String> names = new ArrayList<>(slots.size());
						for (SlotType slot : slots) {
							names.add(slot.name().toLowerCase());
						}
						return names;
					}
			),
			FieldAttachData::slots,
			FieldAttachData::new
	);
}
