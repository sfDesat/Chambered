package com.chambered.action;

import java.util.Optional;

import com.chambered.anim.GunAnimations;
import com.chambered.component.AmmoContents;
import com.chambered.component.GunState;
import com.chambered.data.GunDefinition;
import com.chambered.data.GunDefinitions;
import com.chambered.data.MagazineDefinition;
import com.chambered.item.GunItem;
import com.chambered.item.MagazineItem;
import com.chambered.registry.ModComponents;
import com.chambered.stats.ResolvedGunStats;

import com.geckolib.animatable.GeoItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Preloaded magazine swap as a timed {@link ActionTimeline} action.
 * Mag is reserved at begin; gun state mutates on complete (slide release / rack included).
 */
public final class ReloadHandler {
	private ReloadHandler() {
	}

	public static void handle(ServerPlayer player) {
		ItemStack gunStack = player.getMainHandItem();
		if (!(gunStack.getItem() instanceof GunItem gunItem)) {
			return;
		}
		if (ActionTimeline.isBusy(player)) {
			return;
		}

		Identifier gunId = BuiltInRegistries.ITEM.getKey(gunStack.getItem());
		Optional<GunDefinition> gunDefOpt = GunDefinitions.gun(gunId);
		if (gunDefOpt.isEmpty()) {
			return;
		}
		GunDefinition gunDef = gunDefOpt.get();
		GunState state = gunItem.getGunState(gunStack);
		ResolvedGunStats stats = ResolvedGunStats.of(gunDef, state, 0.0f);

		MagSlot found = findCompatibleMagazine(player, gunDef);
		if (found == null && !state.hasMagazine()) {
			return;
		}

		Optional<AmmoContents> ejectedMag = state.magazine();
		Optional<Identifier> ejectedItem = state.magazineItem();

		Optional<AmmoContents> insertMag = Optional.empty();
		Optional<Identifier> insertItem = Optional.empty();
		ItemStack reserved = ItemStack.EMPTY;

		if (found != null) {
			insertMag = Optional.of(found.contents());
			insertItem = Optional.of(found.magId());
			reserved = player.getInventory().removeItem(found.slot(), 1);
			if (reserved.isEmpty()) {
				return;
			}
		}

		long completeAt = player.level().getGameTime() + stats.reloadTicks();
		boolean started = ActionTimeline.begin(
				player,
				new ActionTimeline.PendingReload(
						completeAt,
						gunId,
						ejectedMag,
						ejectedItem,
						insertMag,
						insertItem,
						reserved
				)
		);
		if (!started) {
			if (!reserved.isEmpty()) {
				player.getInventory().add(reserved);
			}
			return;
		}

		GunSounds.playReload(player, gunDef);
		if (player.level() instanceof ServerLevel serverLevel) {
			gunItem.triggerAnim(
					player,
					GeoItem.getOrAssignId(gunStack, serverLevel),
					GunAnimations.CONTROLLER,
					"reload"
			);
		}
	}

	/**
	 * Prefer the first accepted mag with rounds; otherwise the first empty accepted mag.
	 * Compatibility is gun {@code accepted_magazines} only (single source of truth).
	 */
	private static MagSlot findCompatibleMagazine(ServerPlayer player, GunDefinition gunDef) {
		Inventory inventory = player.getInventory();
		MagSlot firstEmpty = null;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!(stack.getItem() instanceof MagazineItem)) {
				continue;
			}
			Identifier magId = BuiltInRegistries.ITEM.getKey(stack.getItem());
			if (!gunDef.acceptsMagazine(magId)) {
				continue;
			}
			Optional<MagazineDefinition> magDefOpt = GunDefinitions.magazine(magId);
			AmmoContents contents = stack.get(ModComponents.AMMO_CONTENTS);
			if (contents == null) {
				if (magDefOpt.isEmpty()) {
					continue;
				}
				MagazineDefinition magDef = magDefOpt.get();
				contents = new AmmoContents(magDef.capacity(), magDef.capacity(), magDef.caliber(), magDef.defaultAmmoType());
			}
			if (!contents.caliber().equals(gunDef.caliber())) {
				continue;
			}
			if (!contents.isEmpty()) {
				return new MagSlot(i, magId, contents);
			}
			if (firstEmpty == null) {
				firstEmpty = new MagSlot(i, magId, contents);
			}
		}
		return firstEmpty;
	}

	private record MagSlot(int slot, Identifier magId, AmmoContents contents) {
	}
}
