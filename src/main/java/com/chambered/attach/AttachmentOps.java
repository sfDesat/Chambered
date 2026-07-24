package com.chambered.attach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.chambered.component.GunState;
import com.chambered.data.AttachmentDefinition;
import com.chambered.data.GunDefinition;
import com.chambered.data.GunDefinitions;
import com.chambered.item.AttachmentItem;
import com.chambered.item.GunItem;
import com.chambered.registry.ModItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Shared attach / detach validation and GunState writes.
 */
public final class AttachmentOps {
	private AttachmentOps() {
	}

	public static Optional<GunDefinition> gunDef(ItemStack gunStack) {
		if (!(gunStack.getItem() instanceof GunItem gunItem)) {
			return Optional.empty();
		}
		return GunDefinitions.gun(gunItem.getDefinitionId());
	}

	public static boolean gunHasSlot(GunDefinition def, SlotType slot) {
		return def.slots().contains(slot);
	}

	public static Optional<AttachmentDefinition> definitionOf(ItemStack stack) {
		if (!(stack.getItem() instanceof AttachmentItem attachmentItem)) {
			return Optional.empty();
		}
		return GunDefinitions.attachment(attachmentItem.getDefinitionId());
	}

	public static boolean canInstall(
			AttachContext context,
			ItemStack gunStack,
			SlotType slot,
			ItemStack attachmentStack
	) {
		Optional<GunDefinition> gunDef = gunDef(gunStack);
		if (gunDef.isEmpty() || !gunHasSlot(gunDef.get(), slot)) {
			return false;
		}
		Optional<AttachmentDefinition> attachDef = definitionOf(attachmentStack);
		if (attachDef.isEmpty()) {
			return false;
		}
		AttachmentDefinition def = attachDef.get();
		if (def.slot() != slot) {
			return false;
		}
		if (!context.allowsAttachment(def)) {
			return false;
		}
		Identifier gunId = ((GunItem) gunStack.getItem()).getDefinitionId();
		return def.fitsGun(gunId);
	}

	public static ItemStack createAttachmentStack(Identifier attachmentId) {
		return BuiltInRegistries.ITEM.getOptional(attachmentId)
				.map(ItemStack::new)
				.orElse(ItemStack.EMPTY);
	}

	public static Map<SlotType, ItemStack> stacksFromState(GunState state, List<SlotType> slots) {
		Map<SlotType, ItemStack> map = new HashMap<>();
		for (SlotType slot : slots) {
			Identifier id = state.attachments().get(slot);
			map.put(slot, id != null ? createAttachmentStack(id) : ItemStack.EMPTY);
		}
		return map;
	}

	public static GunState withSlotAttachment(GunState state, SlotType slot, ItemStack stack) {
		Map<SlotType, Identifier> next = new HashMap<>(state.attachments());
		if (stack.isEmpty()) {
			next.remove(slot);
		} else {
			Optional<AttachmentDefinition> def = definitionOf(stack);
			if (def.isEmpty()) {
				next.remove(slot);
			} else {
				next.put(slot, ((AttachmentItem) stack.getItem()).getDefinitionId());
			}
		}
		return state.withAttachments(next);
	}

	/** Creative helpers — one of each sample attachment. */
	public static void acceptCreativeAttachments(java.util.function.Consumer<ItemStack> output) {
		output.accept(new ItemStack(ModItems.PISTOL_SUPPRESSOR));
		output.accept(new ItemStack(ModItems.RED_DOT));
		output.accept(new ItemStack(ModItems.FOREGRIP));
		output.accept(new ItemStack(ModItems.WEAPON_LIGHT));
		output.accept(new ItemStack(ModItems.FOLDING_STOCK));
	}
}
