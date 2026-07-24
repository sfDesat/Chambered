package com.chambered.network;

import com.chambered.ChamberedMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → server: open the field attachment menu for the held gun. */
public record ServerboundOpenFieldAttachPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ServerboundOpenFieldAttachPayload> TYPE =
			new CustomPacketPayload.Type<>(ChamberedMod.id("open_field_attach"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenFieldAttachPayload> CODEC =
			StreamCodec.unit(new ServerboundOpenFieldAttachPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
