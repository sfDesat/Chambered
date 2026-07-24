package com.chambered.action;

import java.util.List;

import com.chambered.attach.FieldAttachData;
import com.chambered.attach.SlotType;
import com.chambered.data.GunDefinition;
import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.menu.FieldAttachMenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Opens the field attach menu for the gun in the player's main hand.
 */
public final class FieldAttachHandler {
	private FieldAttachHandler() {
	}

	public static void open(ServerPlayer player) {
		ItemStack main = player.getMainHandItem();
		if (!(main.getItem() instanceof GunItem gunItem)) {
			return;
		}
		GunDefinition def = GunDefinitions.gun(gunItem.getDefinitionId()).orElse(null);
		if (def == null || def.slots().isEmpty()) {
			return;
		}

		List<SlotType> slots = List.copyOf(def.slots());
		FieldAttachData data = new FieldAttachData(InteractionHand.MAIN_HAND, slots);

		player.openMenu(new ExtendedMenuProvider<FieldAttachData>() {
			@Override
			public FieldAttachData getScreenOpeningData(ServerPlayer serverPlayer) {
				return data;
			}

			@Override
			public Component getDisplayName() {
				return Component.translatable("container.chambered.field_attach");
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
				return new FieldAttachMenu(containerId, inventory, data);
			}
		});
	}
}
