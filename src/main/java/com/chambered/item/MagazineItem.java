package com.chambered.item;

import java.util.function.Consumer;

import com.chambered.component.AmmoContents;
import com.chambered.data.MagCompatibility;
import com.chambered.registry.ModComponents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Magazine item. Preloaded mags only in early phases; loose-round loading comes later.
 * Durability-style bar shows remaining rounds (full = full mag, empty = depleted).
 */
public class MagazineItem extends Item {
	public MagazineItem(Properties properties) {
		super(properties);
	}

	public Identifier getDefinitionId() {
		return BuiltInRegistries.ITEM.getKey(this);
	}

	public AmmoContents getAmmoContents(ItemStack stack) {
		return stack.get(ModComponents.AMMO_CONTENTS);
	}

	public void setAmmoContents(ItemStack stack, AmmoContents contents) {
		stack.set(ModComponents.AMMO_CONTENTS, contents);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return getAmmoContents(stack) != null;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		AmmoContents contents = getAmmoContents(stack);
		if (contents == null || contents.capacity() <= 0) {
			return 0;
		}
		return Math.round(MAX_BAR_WIDTH * ((float) contents.count() / (float) contents.capacity()));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		AmmoContents contents = getAmmoContents(stack);
		float fill = 0.0f;
		if (contents != null && contents.capacity() > 0) {
			fill = (float) contents.count() / (float) contents.capacity();
		}
		// Same green→red HSV ramp vanilla tools use for remaining durability.
		return Mth.hsvToRgb(fill / 3.0f, 1.0f, 1.0f);
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> consumer,
			TooltipFlag flag
	) {
		AmmoContents contents = getAmmoContents(stack);
		if (contents != null) {
			consumer.accept(Component.literal(contents.count() + "/" + contents.capacity() + " rounds"));
			consumer.accept(Component.literal("Caliber: " + contents.caliber()));
			consumer.accept(Component.literal("Ammo: " + contents.ammoType()));
		}
		MagCompatibility.appendFitsGuns(getDefinitionId(), consumer);
	}
}
