package com.chambered.action;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.stats.ResolvedGunStats;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side draw / equip gate so fire is blocked until draw time elapses (mirrors client DrawState).
 */
public final class EquipGates {
	private static final Map<UUID, Item> LAST_ITEM = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> READY_AT = new ConcurrentHashMap<>();

	private EquipGates() {
	}

	public static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		ItemStack main = player.getMainHandItem();
		Item current = main.getItem();
		Item last = LAST_ITEM.put(player.getUUID(), current);
		if (current == last) {
			return;
		}
		if (current instanceof GunItem) {
			float drawSec = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(current))
					.map(def -> ResolvedGunStats.of(def, ((GunItem) current).getGunState(main), 0.0f).drawTime())
					.orElse(0.35f);
			READY_AT.put(player.getUUID(), player.level().getGameTime() + Math.max(1, Math.round(drawSec * 20.0f)));
		} else {
			READY_AT.remove(player.getUUID());
		}
	}

	public static boolean canFire(ServerPlayer player) {
		Long readyAt = READY_AT.get(player.getUUID());
		if (readyAt == null) {
			return true;
		}
		return player.level().getGameTime() >= readyAt;
	}

	public static void clear(ServerPlayer player) {
		LAST_ITEM.remove(player.getUUID());
		READY_AT.remove(player.getUUID());
	}
}
