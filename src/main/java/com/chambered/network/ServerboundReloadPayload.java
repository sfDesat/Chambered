package com.chambered.network;

import com.chambered.ChamberedMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client → server reload request. Mag insert/chamber logic lands in Phase 1.
 */
public record ServerboundReloadPayload() implements CustomPacketPayload {
	public static final Identifier ID = ChamberedMod.id("reload");
	public static final Type<ServerboundReloadPayload> TYPE = new Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundReloadPayload> CODEC =
			StreamCodec.unit(new ServerboundReloadPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
