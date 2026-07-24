package com.chambered.action;

import com.chambered.component.FireMode;

import net.minecraft.server.level.ServerPlayer;

/**
 * Entry points for serverbound gun actions.
 */
public final class Actions {
	private Actions() {
	}

	public static void fire(ServerPlayer player, boolean aiming) {
		FireHandler.handle(player, aiming);
	}

	public static void reload(ServerPlayer player) {
		ReloadHandler.handle(player);
	}

	public static void changeFireMode(ServerPlayer player, FireMode mode) {
		FireModeHandler.handle(player, mode);
	}

	public static void cycleFireMode(ServerPlayer player) {
		FireModeHandler.cycle(player);
	}

	public static void openFieldAttach(ServerPlayer player) {
		FieldAttachHandler.open(player);
	}

	public static void fieldAttachSelect(ServerPlayer player, com.chambered.attach.SlotType slot, int inventorySlot) {
		FieldAttachSelectHandler.handle(player, slot, inventorySlot);
	}
}
