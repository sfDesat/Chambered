package com.chambered.action;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player fire cooldown keyed by game time.
 */
public final class GunCooldowns {
	private static final Map<UUID, Long> LAST_FIRE_TICK = new ConcurrentHashMap<>();

	private GunCooldowns() {
	}

	public static boolean tryAcquire(ServerPlayer player, int intervalTicks) {
		long now = player.level().getGameTime();
		Long last = LAST_FIRE_TICK.get(player.getUUID());
		if (last != null && now - last < intervalTicks) {
			return false;
		}
		LAST_FIRE_TICK.put(player.getUUID(), now);
		return true;
	}

	public static void clear(ServerPlayer player) {
		LAST_FIRE_TICK.remove(player.getUUID());
	}
}
