package com.chambered.action;

import java.util.List;
import java.util.Optional;

import com.chambered.component.FireMode;
import com.chambered.component.GunState;
import com.chambered.data.GunDefinition;
import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Cycles or sets fire mode among modes allowed by the gun definition.
 */
public final class FireModeHandler {
	private FireModeHandler() {
	}

	public static void handle(ServerPlayer player, FireMode requested) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gunItem)) {
			return;
		}
		if (ActionTimeline.isBusy(player)) {
			return;
		}

		Identifier gunId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		Optional<GunDefinition> gunDefOpt = GunDefinitions.gun(gunId);
		if (gunDefOpt.isEmpty()) {
			return;
		}
		GunDefinition gunDef = gunDefOpt.get();
		GunState state = gunItem.getGunState(stack);

		FireMode next;
		if (requested != null && gunDef.allowsFireMode(requested)) {
			next = requested;
		} else {
			next = cycleNext(gunDef.fireModes(), state.fireMode());
		}

		if (next == state.fireMode()) {
			player.sendOverlayMessage(
					Component.literal("Fire mode: " + next.name().toLowerCase() + " (only mode)")
			);
			return;
		}

		gunItem.setGunState(stack, state.withFireMode(next));
		player.sendOverlayMessage(Component.literal("Fire mode: " + next.name().toLowerCase()));
		GunSounds.playFireMode(player, gunDef);
	}

	public static void cycle(ServerPlayer player) {
		handle(player, null);
	}

	private static FireMode cycleNext(List<FireMode> allowed, FireMode current) {
		if (allowed.isEmpty()) {
			return FireMode.SEMI;
		}
		int index = allowed.indexOf(current);
		if (index < 0) {
			return allowed.getFirst();
		}
		return allowed.get((index + 1) % allowed.size());
	}
}
