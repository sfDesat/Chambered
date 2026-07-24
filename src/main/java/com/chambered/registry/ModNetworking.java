package com.chambered.registry;

import com.chambered.ChamberedMod;
import com.chambered.action.Actions;
import com.chambered.network.ServerboundChangeFireModePayload;
import com.chambered.network.ServerboundFieldAttachSelectPayload;
import com.chambered.network.ServerboundFirePayload;
import com.chambered.network.ServerboundOpenFieldAttachPayload;
import com.chambered.network.ServerboundReloadPayload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundFirePayload.TYPE, ServerboundFirePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundReloadPayload.TYPE, ServerboundReloadPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundChangeFireModePayload.TYPE, ServerboundChangeFireModePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundOpenFieldAttachPayload.TYPE, ServerboundOpenFieldAttachPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundFieldAttachSelectPayload.TYPE, ServerboundFieldAttachSelectPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ServerboundFirePayload.TYPE, (payload, context) ->
				context.server().execute(() -> Actions.fire(context.player(), payload.aiming()))
		);
		ServerPlayNetworking.registerGlobalReceiver(ServerboundReloadPayload.TYPE, (payload, context) ->
				context.server().execute(() -> Actions.reload(context.player()))
		);
		ServerPlayNetworking.registerGlobalReceiver(ServerboundChangeFireModePayload.TYPE, (payload, context) ->
				context.server().execute(() -> {
					if (payload.fireMode().isPresent()) {
						Actions.changeFireMode(context.player(), payload.fireMode().get());
					} else {
						Actions.cycleFireMode(context.player());
					}
				})
		);
		ServerPlayNetworking.registerGlobalReceiver(ServerboundOpenFieldAttachPayload.TYPE, (payload, context) ->
				context.server().execute(() -> Actions.openFieldAttach(context.player()))
		);
		ServerPlayNetworking.registerGlobalReceiver(ServerboundFieldAttachSelectPayload.TYPE, (payload, context) ->
				context.server().execute(() ->
						Actions.fieldAttachSelect(context.player(), payload.slot(), payload.inventorySlot())
				)
		);

		ChamberedMod.LOGGER.info("Registered networking");
	}
}
