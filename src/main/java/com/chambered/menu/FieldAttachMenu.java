package com.chambered.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chambered.attach.AttachContext;
import com.chambered.attach.AttachmentOps;
import com.chambered.attach.FieldAttachData;
import com.chambered.attach.SlotType;
import com.chambered.component.GunState;
import com.chambered.item.AttachmentItem;
import com.chambered.item.GunItem;
import com.chambered.registry.ModMenuTypes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Field attachment menu: gun-specific rail slots only (no player inventory grid).
 * Attachments are chosen from a filtered inventory list on the client.
 */
public class FieldAttachMenu extends AbstractContainerMenu {
	/** Compact canvas — must fit large GUI scales (optic slot stays inside top edge). */
	public static final int PANEL_WIDTH = 320;
	public static final int PANEL_HEIGHT = 180;
	public static final int GUN_CENTER_X = 160;
	public static final int GUN_CENTER_Y = 96;

	private final FieldAttachData data;
	private final Container attachmentContainer;
	private final int attachmentSlotCount;
	private final Inventory playerInventory;
	private boolean syncing;

	public FieldAttachMenu(int containerId, Inventory inventory, FieldAttachData data) {
		super(ModMenuTypes.FIELD_ATTACH, containerId);
		this.data = data;
		this.playerInventory = inventory;
		this.attachmentSlotCount = data.slots().size();

		this.attachmentContainer = new SimpleContainer(Math.max(1, attachmentSlotCount)) {
			@Override
			public void setChanged() {
				super.setChanged();
				if (!syncing) {
					FieldAttachMenu.this.writeAttachmentsToGun(inventory.player);
				}
			}
		};

		seedAttachments(inventory.player);

		List<SlotType> slots = data.slots();
		for (int i = 0; i < slots.size(); i++) {
			SlotType slotType = slots.get(i);
			int x = GUN_CENTER_X + slotType.menuOffsetX() - 8;
			int y = GUN_CENTER_Y + slotType.menuOffsetY() - 8;
			addSlot(new AttachmentSlot(attachmentContainer, i, x, y, slotType, inventory.player));
		}
	}

	public FieldAttachData data() {
		return data;
	}

	public List<SlotType> slots() {
		return data.slots();
	}

	public int attachmentSlotCount() {
		return attachmentSlotCount;
	}

	public Inventory playerInventory() {
		return playerInventory;
	}

	public ItemStack gunStack() {
		return playerInventory.player.getItemInHand(data.hand());
	}

	public int indexOf(SlotType slotType) {
		List<SlotType> slots = data.slots();
		for (int i = 0; i < slots.size(); i++) {
			if (slots.get(i) == slotType) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Install attachment from a player inventory slot, or unequip when {@code inventorySlot < 0}.
	 */
	public boolean selectAttachment(Player player, SlotType slotType, int inventorySlot) {
		int attachIndex = indexOf(slotType);
		if (attachIndex < 0) {
			return false;
		}
		ItemStack gun = gunStack();
		if (!(gun.getItem() instanceof GunItem)) {
			return false;
		}

		ItemStack currentlyMounted = attachmentContainer.getItem(attachIndex).copy();

		if (inventorySlot < 0) {
			if (currentlyMounted.isEmpty()) {
				return false;
			}
			attachmentContainer.setItem(attachIndex, ItemStack.EMPTY);
			if (!player.getInventory().add(currentlyMounted)) {
				player.drop(currentlyMounted, false);
			}
			writeAttachmentsToGun(player);
			syncAfterAttachChange(player);
			return true;
		}

		ItemStack fromInv = player.getInventory().getItem(inventorySlot);
		if (!AttachmentOps.canInstall(AttachContext.FIELD, gun, slotType, fromInv)) {
			return false;
		}

		ItemStack toInstall = fromInv.split(1);
		player.getInventory().setItem(inventorySlot, fromInv);
		if (!currentlyMounted.isEmpty()) {
			if (!player.getInventory().add(currentlyMounted)) {
				player.drop(currentlyMounted, false);
			}
		}
		attachmentContainer.setItem(attachIndex, toInstall);
		writeAttachmentsToGun(player);
		syncAfterAttachChange(player);
		return true;
	}

	/**
	 * Attach slots are menu-synced via {@link #broadcastChanges()}, but player inventory
	 * is not part of this menu — sync {@link ServerPlayer#inventoryMenu} immediately so
	 * items leave/return without waiting for menu close (avoids client-side dupes).
	 */
	private void syncAfterAttachChange(Player player) {
		broadcastChanges();
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.inventoryMenu.broadcastChanges();
		}
	}

	private void seedAttachments(Player player) {
		ItemStack gunStack = player.getItemInHand(data.hand());
		if (!(gunStack.getItem() instanceof GunItem gunItem)) {
			return;
		}
		GunState state = gunItem.getGunState(gunStack);
		syncing = true;
		try {
			List<SlotType> slots = data.slots();
			for (int i = 0; i < slots.size(); i++) {
				ItemStack attach = AttachmentOps.stacksFromState(state, List.of(slots.get(i))).get(slots.get(i));
				attachmentContainer.setItem(i, attach == null ? ItemStack.EMPTY : attach);
			}
		} finally {
			syncing = false;
		}
	}

	private void writeAttachmentsToGun(Player player) {
		ItemStack gunStack = player.getItemInHand(data.hand());
		if (!(gunStack.getItem() instanceof GunItem gunItem)) {
			return;
		}
		GunState state = gunItem.getGunState(gunStack);
		Map<SlotType, net.minecraft.resources.Identifier> map = new HashMap<>(state.attachments());
		List<SlotType> slots = data.slots();
		for (int i = 0; i < slots.size(); i++) {
			SlotType slot = slots.get(i);
			ItemStack stack = attachmentContainer.getItem(i);
			if (stack.isEmpty()) {
				map.remove(slot);
			} else if (AttachmentOps.canInstall(AttachContext.FIELD, gunStack, slot, stack)) {
				AttachmentOps.definitionOf(stack).ifPresent(def ->
						map.put(slot, ((AttachmentItem) stack.getItem()).getDefinitionId())
				);
			}
		}
		gunItem.setGunState(gunStack, state.withAttachments(map));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
		// Selection is handled via ServerboundFieldAttachSelectPayload — ignore vanilla slot clicks.
	}

	@Override
	public boolean stillValid(Player player) {
		return player.getItemInHand(data.hand()).getItem() instanceof GunItem;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		writeAttachmentsToGun(player);
	}

	public final class AttachmentSlot extends Slot {
		private final SlotType slotType;
		private final Player player;

		private AttachmentSlot(Container container, int index, int x, int y, SlotType slotType, Player player) {
			super(container, index, x, y);
			this.slotType = slotType;
			this.player = player;
		}

		public SlotType slotType() {
			return slotType;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			ItemStack gun = player.getItemInHand(data.hand());
			return AttachmentOps.canInstall(AttachContext.FIELD, gun, slotType, stack);
		}

		@Override
		public boolean mayPickup(Player player) {
			return false;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}
}
