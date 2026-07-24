package com.chambered.network;

import com.chambered.ChamberedMod;
import com.chambered.attach.SlotType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server: install an inventory attachment into a rail slot, or unequip ({@code inventorySlot < 0}).
 */
public record ServerboundFieldAttachSelectPayload(SlotType slot, int inventorySlot) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ServerboundFieldAttachSelectPayload> TYPE =
			new CustomPacketPayload.Type<>(ChamberedMod.id("field_attach_select"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFieldAttachSelectPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8.map(
							s -> SlotType.valueOf(s.toUpperCase()),
							t -> t.name().toLowerCase()
					),
					ServerboundFieldAttachSelectPayload::slot,
					ByteBufCodecs.VAR_INT,
					ServerboundFieldAttachSelectPayload::inventorySlot,
					ServerboundFieldAttachSelectPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
