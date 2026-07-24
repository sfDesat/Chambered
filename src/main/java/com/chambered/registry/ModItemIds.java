package com.chambered.registry;

import com.chambered.ChamberedMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
	public static final ResourceKey<Item> MAKAROV = create("makarov");
	public static final ResourceKey<Item> MAKAROV_MAGAZINE = create("makarov_magazine");
	public static final ResourceKey<Item> PISTOL_SUPPRESSOR = create("pistol_suppressor");
	public static final ResourceKey<Item> RED_DOT = create("red_dot");
	public static final ResourceKey<Item> FOREGRIP = create("foregrip");
	public static final ResourceKey<Item> WEAPON_LIGHT = create("weapon_light");
	public static final ResourceKey<Item> FOLDING_STOCK = create("folding_stock");

	private ModItemIds() {
	}

	public static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ChamberedMod.MOD_ID, name));
	}
}
