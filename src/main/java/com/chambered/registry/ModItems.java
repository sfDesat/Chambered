package com.chambered.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.chambered.ChamberedMod;
import com.chambered.attach.AttachmentOps;
import com.chambered.component.AmmoContents;
import com.chambered.component.GunState;
import com.chambered.content.GunContent;
import com.chambered.content.MagContent;
import com.chambered.item.AttachmentItem;
import com.chambered.item.GunItem;
import com.chambered.item.MagazineItem;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModItems {
	public static final Map<String, Item> GUNS;
	public static final Map<String, Item> MAGAZINES;

	public static final Item MAKAROV;
	public static final Item MAKAROV_MAGAZINE;

	static {
		Map<String, Item> guns = new LinkedHashMap<>();
		Map<String, Item> magazines = new LinkedHashMap<>();
		for (GunContent.Entry entry : GunContent.ENTRIES) {
			guns.put(entry.id(), register(
					ModItemIds.create(entry.id()),
					GunItem::new,
					new Item.Properties()
							.stacksTo(1)
							.component(ModComponents.GUN_STATE, GunState.DEFAULT)
			));
		}
		for (MagContent.Entry entry : MagContent.ENTRIES) {
			magazines.put(entry.id(), register(
					ModItemIds.create(entry.id()),
					MagazineItem::new,
					new Item.Properties()
							.stacksTo(1)
							.component(
									ModComponents.AMMO_CONTENTS,
									new AmmoContents(
											entry.capacity(),
											entry.capacity(),
											ChamberedMod.id(entry.caliberId()),
											ChamberedMod.id(entry.ammoTypeId())
									)
							)
			));
		}
		GUNS = Collections.unmodifiableMap(guns);
		MAGAZINES = Collections.unmodifiableMap(magazines);
		MAKAROV = guns.get("makarov");
		MAKAROV_MAGAZINE = magazines.get("makarov_magazine");
	}

	public static final Item PISTOL_SUPPRESSOR = register(
			ModItemIds.PISTOL_SUPPRESSOR,
			AttachmentItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static final Item RED_DOT = register(
			ModItemIds.RED_DOT,
			AttachmentItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static final Item FOREGRIP = register(
			ModItemIds.FOREGRIP,
			AttachmentItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static final Item WEAPON_LIGHT = register(
			ModItemIds.WEAPON_LIGHT,
			AttachmentItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static final Item FOLDING_STOCK = register(
			ModItemIds.FOLDING_STOCK,
			AttachmentItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static final ResourceKey<CreativeModeTab> GUNS_TAB_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			ChamberedMod.id("guns")
	);

	public static final CreativeModeTab GUNS_TAB = FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(MAKAROV))
			.title(Component.translatable("itemGroup.chambered.guns"))
			.displayItems((params, output) -> {
				for (Item gun : GUNS.values()) {
					output.accept(gun);
				}
			})
			.build();

	public static final ResourceKey<CreativeModeTab> AMMO_TAB_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			ChamberedMod.id("guns_ammo")
	);

	public static final CreativeModeTab AMMO_TAB = FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(MAKAROV_MAGAZINE))
			.title(Component.translatable("itemGroup.chambered.ammo"))
			.displayItems((params, output) -> {
				for (Item mag : MAGAZINES.values()) {
					output.accept(mag);
				}
			})
			.build();

	public static final ResourceKey<CreativeModeTab> ATTACHMENTS_TAB_KEY = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB,
			ChamberedMod.id("guns_attachments")
	);

	public static final CreativeModeTab ATTACHMENTS_TAB = FabricCreativeModeTab.builder()
			.icon(() -> new ItemStack(RED_DOT))
			.title(Component.translatable("itemGroup.chambered.attachments"))
			.displayItems((params, output) -> {
				output.accept(ModBlocks.GUN_WORKBENCH.asItem());
				AttachmentOps.acceptCreativeAttachments(output::accept);
			})
			.build();

	private ModItems() {
	}

	private static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> factory, Item.Properties properties) {
		Item item = factory.apply(properties.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, GUNS_TAB_KEY, GUNS_TAB);
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, AMMO_TAB_KEY, AMMO_TAB);
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ATTACHMENTS_TAB_KEY, ATTACHMENTS_TAB);
		ChamberedMod.LOGGER.info("Registered items ({} guns, {} magazines)", GUNS.size(), MAGAZINES.size());
	}
}
