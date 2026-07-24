package com.chambered.data;

import java.util.ArrayList;
import java.util.List;

import com.chambered.ChamberedMod;
import com.chambered.content.GunContent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * Inventory tooltip helpers for mag ↔ gun compatibility.
 * Prefers loaded datapack definitions; falls back to {@link GunContent} roster.
 */
public final class MagCompatibility {
	private MagCompatibility() {
	}

	public static List<Identifier> gunsAccepting(Identifier magazineId) {
		if (!GunDefinitions.guns().isEmpty()) {
			List<Identifier> result = new ArrayList<>();
			for (var entry : GunDefinitions.guns().entrySet()) {
				if (entry.getValue().acceptsMagazine(magazineId)) {
					result.add(entry.getKey());
				}
			}
			return result;
		}
		String path = magazineId.getPath();
		List<Identifier> result = new ArrayList<>();
		for (GunContent.Entry gun : GunContent.ENTRIES) {
			if (gun.acceptedMagazines().contains(path)) {
				result.add(ChamberedMod.id(gun.id()));
			}
		}
		return result;
	}

	public static List<Identifier> magazinesAcceptedBy(Identifier gunId) {
		return GunDefinitions.gun(gunId)
				.map(GunDefinition::acceptedMagazines)
				.orElseGet(() -> {
					for (GunContent.Entry gun : GunContent.ENTRIES) {
						if (gun.id().equals(gunId.getPath())) {
							List<Identifier> ids = new ArrayList<>(gun.acceptedMagazines().size());
							for (String magId : gun.acceptedMagazines()) {
								ids.add(ChamberedMod.id(magId));
							}
							return ids;
						}
					}
					return List.of();
				});
	}

	public static void appendFitsGuns(Identifier magazineId, java.util.function.Consumer<Component> consumer) {
		List<Identifier> guns = gunsAccepting(magazineId);
		if (guns.isEmpty()) {
			return;
		}
		consumer.accept(Component.translatable("tooltip.chambered.magazine.fits").withStyle(ChatFormatting.GRAY));
		for (Identifier gunId : guns) {
			consumer.accept(Component.literal("  ").append(itemName(gunId)).withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	public static void appendAcceptedMags(Identifier gunId, java.util.function.Consumer<Component> consumer) {
		List<Identifier> mags = magazinesAcceptedBy(gunId);
		if (mags.isEmpty()) {
			return;
		}
		consumer.accept(Component.translatable("tooltip.chambered.gun.magazines").withStyle(ChatFormatting.GRAY));
		for (Identifier magId : mags) {
			consumer.accept(Component.literal("  ").append(itemName(magId)).withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	private static Component itemName(Identifier id) {
		return Component.translatable(Util.makeDescriptionId("item", id));
	}
}
