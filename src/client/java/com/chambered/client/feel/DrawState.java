package com.chambered.client.feel;

import com.chambered.component.GunState;
import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.stats.ResolvedGunStats;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side draw gate — blocks fire until draw time elapses after equipping a gun.
 */
public final class DrawState {
	private static Item lastItem;
	private static long readyAtGameTime = Long.MIN_VALUE / 4;
	private static boolean drawing;
	private static boolean drawAnimPending;

	private DrawState() {
	}

	public static boolean canFire(Minecraft client) {
		if (client.level == null) {
			return false;
		}
		return !drawing && client.level.getGameTime() >= readyAtGameTime;
	}

	public static boolean isDrawing() {
		return drawing;
	}

	/** True once when a draw begins — caller should trigger the draw clip. */
	public static boolean consumeDrawAnim() {
		if (!drawAnimPending) {
			return false;
		}
		drawAnimPending = false;
		return true;
	}

	public static void tick(Minecraft client, boolean holdingGun) {
		if (client.player == null || client.level == null) {
			return;
		}

		ItemStack main = client.player.getMainHandItem();
		Item current = main.getItem();
		long now = client.level.getGameTime();

		if (current != lastItem) {
			lastItem = current;
			if (holdingGun && current instanceof GunItem gunItem) {
				float drawSec = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(current))
						.map(def -> ResolvedGunStats.of(def, gunItem.getGunState(main), 0.0f).drawTime())
						.orElse(0.35f);
				readyAtGameTime = now + Math.max(1, Math.round(drawSec * 20.0f));
				drawing = true;
				drawAnimPending = true;
			} else {
				drawing = false;
				drawAnimPending = false;
				readyAtGameTime = Long.MIN_VALUE / 4;
			}
		}

		if (drawing && now >= readyAtGameTime) {
			drawing = false;
		}

		if (!holdingGun) {
			drawing = false;
		}
	}
}
