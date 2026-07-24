package com.chambered.network;

import com.chambered.ChamberedMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client → server fire request. {@code aiming} selects hip vs ADS spread.
 */
public record ServerboundFirePayload(boolean aiming) implements CustomPacketPayload {
	public static final Identifier ID = ChamberedMod.id("fire");
	public static final Type<ServerboundFirePayload> TYPE = new Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFirePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			ServerboundFirePayload::aiming,
			ServerboundFirePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
