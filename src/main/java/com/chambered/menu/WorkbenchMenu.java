package com.chambered.menu;

import com.chambered.component.GunState;
import com.chambered.item.GunItem;
import com.chambered.registry.ModBlocks;
import com.chambered.registry.ModMenuTypes;

import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Gun workbench: insert a firearm + iron ingot to restore condition.
 */
public class WorkbenchMenu extends AbstractContainerMenu {
	public static final int GUN_SLOT = 0;
	public static final int MATERIAL_SLOT = 1;

	private final ContainerLevelAccess access;
	private final Container inputs = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			WorkbenchMenu.this.tryRepair();
		}
	};

	public WorkbenchMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	public WorkbenchMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenuTypes.GUN_WORKBENCH, containerId);
		this.access = access;

		addSlot(new Slot(inputs, GUN_SLOT, 44, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof GunItem;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		addSlot(new Slot(inputs, MATERIAL_SLOT, 80, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(Items.IRON_INGOT);
			}
		});

		addStandardInventorySlots(inventory, 8, 84);
	}

	private void tryRepair() {
		ItemStack gunStack = inputs.getItem(GUN_SLOT);
		ItemStack material = inputs.getItem(MATERIAL_SLOT);
		if (!(gunStack.getItem() instanceof GunItem gunItem) || material.isEmpty() || !material.is(Items.IRON_INGOT)) {
			return;
		}
		GunState state = gunItem.getGunState(gunStack);
		if (state.condition() >= 99.9f) {
			return;
		}
		float missing = 100.0f - state.condition();
		int needed = Math.max(1, Mth.ceil(missing / 100.0f));
		int use = Math.min(needed, material.getCount());
		if (use <= 0) {
			return;
		}
		material.shrink(use);
		inputs.setItem(MATERIAL_SLOT, material);
		gunItem.setGunState(gunStack, state.withCondition(100.0f));
		inputs.setItem(GUN_SLOT, gunStack);
		broadcastChanges();
	}

	public float gunCondition() {
		ItemStack gunStack = inputs.getItem(GUN_SLOT);
		if (!(gunStack.getItem() instanceof GunItem gunItem)) {
			return -1.0f;
		}
		return gunItem.getGunState(gunStack).condition();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(slotIndex);
		if (!slot.hasItem()) {
			return result;
		}

		ItemStack stack = slot.getItem();
		result = stack.copy();

		int invStart = 2;
		int invEnd = slots.size();

		if (slotIndex < invStart) {
			if (!moveItemStackTo(stack, invStart, invEnd, true)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.getItem() instanceof GunItem) {
			if (!moveItemStackTo(stack, GUN_SLOT, GUN_SLOT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.is(Items.IRON_INGOT)) {
			if (!moveItemStackTo(stack, MATERIAL_SLOT, MATERIAL_SLOT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return result;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.GUN_WORKBENCH);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		access.execute((level, pos) -> clearContainer(player, inputs));
	}
}
