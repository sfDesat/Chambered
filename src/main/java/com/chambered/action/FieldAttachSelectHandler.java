package com.chambered.action;

import com.chambered.attach.SlotType;
import com.chambered.menu.FieldAttachMenu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Applies a field-attach pick-list selection on the open menu.
 */
public final class FieldAttachSelectHandler {
	private FieldAttachSelectHandler() {
	}

	public static void handle(ServerPlayer player, SlotType slot, int inventorySlot) {
		AbstractContainerMenu menu = player.containerMenu;
		if (!(menu instanceof FieldAttachMenu fieldMenu)) {
			return;
		}
		fieldMenu.selectAttachment(player, slot, inventorySlot);
	}
}
