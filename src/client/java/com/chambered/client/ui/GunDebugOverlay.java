package com.chambered.client.ui;

import com.chambered.ChamberedMod;
import com.chambered.client.feel.ClientGunFeel;
import com.chambered.component.AmmoContents;
import com.chambered.component.GunState;
import com.chambered.data.GunDefinitions;
import com.chambered.entity.BulletEntity;
import com.chambered.item.GunItem;
import com.chambered.item.MagazineItem;
import com.chambered.registry.ModComponents;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Toggleable debug HUD for gun / mag / definition / bullet state. Bound to F8 by default.
 */
public final class GunDebugOverlay {
	private static boolean visible;

	private GunDebugOverlay() {
	}

	public static void register() {
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.HOTBAR,
				ChamberedMod.id("gun_debug"),
				GunDebugOverlay::render
		);
	}

	public static void toggle() {
		visible = !visible;
	}

	public static boolean isVisible() {
		return visible;
	}

	private static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker delta) {
		if (!visible) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.level == null) {
			return;
		}

		var font = client.font;
		int x = 4;
		int y = 4;
		int color = 0xFFA0E0FF;
		int warn = 0xFFFFAA55;

		BulletStats bullets = countBullets(client);
			int panelHeight = 188;
		graphics.fill(x - 2, y - 2, x + 250, y + panelHeight, 0x80000000);
		graphics.text(font, "Chambered debug (F8)", x, y, color);
		y += 12;

		graphics.text(font, "Bullets: " + bullets.total + " (tracked " + BulletEntity.activeCount() + ")", x, y, color);
		y += 10;
		graphics.text(
				font,
				String.format("  oldest age: %d / hardcap %d", bullets.oldestAge, BulletEntity.HARD_MAX_LIFE_TICKS),
				x,
				y,
				bullets.oldestAge > BulletEntity.HARD_MAX_LIFE_TICKS - 10 ? warn : color
		);
		y += 10;
		graphics.text(font, String.format("  farthest: %.1f blocks", bullets.farthestDist), x, y, color);
		y += 10;
		graphics.text(font, "Shots (client predict): " + ClientGunFeel.shotsThisSession(), x, y, color);
		y += 10;
		int ping = -1;
		if (player.connection != null) {
			var info = player.connection.getPlayerInfo(player.getUUID());
			if (info != null) {
				ping = info.getLatency();
			}
		}
		graphics.text(font, String.format("FPS ~%d | ping %d", client.getFps(), ping), x, y, color);
		y += 12;

		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof GunItem gunItem) {
			GunState state = gunItem.getGunState(stack);
			var gunId = BuiltInRegistries.ITEM.getKey(stack.getItem());
			graphics.text(font, "Gun: " + gunId, x, y, color);
			y += 10;
			graphics.text(font, "Bolt: " + state.bolt().name().toLowerCase(), x, y, color);
			y += 10;
			graphics.text(font, "Mode: " + state.fireMode().name().toLowerCase() + "  (B to cycle)", x, y, color);
			y += 10;
			graphics.text(font, "Chamber: " + state.chamberAmmoType().map(Object::toString).orElse("empty"), x, y, color);
			y += 10;
			if (state.magazine().isPresent()) {
				AmmoContents mag = state.magazine().get();
				graphics.text(font, "Mag: " + mag.count() + "/" + mag.capacity() + " (" + mag.ammoType() + ")", x, y, color);
			} else {
				graphics.text(font, "Mag: none", x, y, color);
			}
			y += 10;
			boolean defLoaded = GunDefinitions.gun(gunId).isPresent();
			graphics.text(font, "Def loaded: " + defLoaded + " (guns=" + GunDefinitions.guns().size() + ")", x, y, color);
			y += 10;
			var resolved = GunDefinitions.gun(gunId).map(def ->
					com.chambered.stats.ResolvedGunStats.of(def, state, com.chambered.client.feel.AdsState.progress())
			);
			if (resolved.isPresent()) {
				var s = resolved.get();
				graphics.text(
						font,
						String.format("Resolved: wt=%.2fkg reload=%dt spread=%.2f", s.weightKg(), s.reloadTicks(), s.effectiveSpread()),
						x,
						y,
						color
				);
				y += 10;
			}
			graphics.text(
					font,
					String.format(
							"Recoil impulse p/y: %.2f / %.2f | unrecovered %.2f",
							ClientGunFeel.recoil().pitch(),
							ClientGunFeel.recoil().yaw(),
							ClientGunFeel.unrecoveredPitch()
					),
					x,
					y,
					color
			);
			y += 10;
			graphics.text(
					font,
					String.format(
							"ADS: %.0f%% (RMB) | aiming=%s | draw=%s",
							com.chambered.client.feel.AdsState.progress() * 100.0f,
							com.chambered.client.feel.AdsState.isAiming(),
							com.chambered.client.feel.DrawState.isDrawing()
					),
					x,
					y,
					color
			);
		} else if (stack.getItem() instanceof MagazineItem) {
			AmmoContents contents = stack.get(ModComponents.AMMO_CONTENTS);
			graphics.text(font, "Holding magazine", x, y, color);
			y += 10;
			if (contents != null) {
				graphics.text(font, contents.count() + "/" + contents.capacity() + " " + contents.ammoType(), x, y, color);
			}
		} else {
			graphics.text(font, "Hold a Chambered gun/mag", x, y, color);
			y += 10;
			graphics.text(font, "LMB fire | RMB ADS | R reload | I inspect | B mode | F8", x, y, color);
		}
	}

	private static BulletStats countBullets(Minecraft client) {
		int total = 0;
		int oldest = 0;
		double farthest = 0.0;
		Vec3 eye = client.player.getEyePosition();
		for (Entity entity : client.level.entitiesForRendering()) {
			if (entity instanceof BulletEntity bullet) {
				total++;
				oldest = Math.max(oldest, bullet.ageTicks());
				farthest = Math.max(farthest, eye.distanceTo(bullet.position()));
			}
		}
		return new BulletStats(total, oldest, farthest);
	}

	private record BulletStats(int total, int oldestAge, double farthestDist) {
	}
}
