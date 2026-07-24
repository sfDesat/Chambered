package com.chambered.network;

import java.util.Optional;

import com.chambered.ChamberedMod;
import com.chambered.component.FireMode;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client → server fire-mode change. Empty optional means "cycle to next allowed mode".
 */
public record ServerboundChangeFireModePayload(Optional<FireMode> fireMode) implements CustomPacketPayload {
	public static final Identifier ID = ChamberedMod.id("change_fire_mode");
	public static final Type<ServerboundChangeFireModePayload> TYPE = new Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChangeFireModePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(
					s -> FireMode.valueOf(s.toUpperCase()),
					mode -> mode.name().toLowerCase()
			)),
			ServerboundChangeFireModePayload::fireMode,
			ServerboundChangeFireModePayload::new
	);

	public static ServerboundChangeFireModePayload cycle() {
		return new ServerboundChangeFireModePayload(Optional.empty());
	}

	public static ServerboundChangeFireModePayload set(FireMode mode) {
		return new ServerboundChangeFireModePayload(Optional.of(mode));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
