package com.chambered.item;

import java.util.function.Consumer;

import com.chambered.attach.SlotType;
import com.chambered.data.AttachmentDefinition;
import com.chambered.data.GunDefinitions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Attachment item. Definition id matches the item registry id.
 */
public class AttachmentItem extends Item {
	public AttachmentItem(Properties properties) {
		super(properties);
	}

	public Identifier getDefinitionId() {
		return BuiltInRegistries.ITEM.getKey(this);
	}

	public AttachmentDefinition getDefinition() {
		return GunDefinitions.attachment(getDefinitionId()).orElse(null);
	}

	public SlotType getSlotType() {
		AttachmentDefinition def = getDefinition();
		return def != null ? def.slot() : null;
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> consumer,
			TooltipFlag flag
	) {
		AttachmentDefinition def = getDefinition();
		if (def == null) {
			return;
		}
		consumer.accept(Component.translatable("tooltip.chambered.attachment.slot", def.slot().getSerializedName()));
		if (def.fieldAttachable()) {
			consumer.accept(Component.translatable("tooltip.chambered.attachment.field"));
		} else {
			consumer.accept(Component.translatable("tooltip.chambered.attachment.bench_only"));
		}
	}
}
