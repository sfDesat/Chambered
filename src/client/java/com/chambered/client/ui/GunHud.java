package com.chambered.client.ui;

import com.chambered.ChamberedMod;
import com.chambered.client.feel.DrawState;
import com.chambered.component.AmmoContents;
import com.chambered.component.GunState;
import com.chambered.item.GunItem;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Always-on ammo readout (bottom-right) while holding a gun, plus draw hint.
 */
public final class GunHud {
	private GunHud() {
	}

	public static void register() {
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.HOTBAR,
				ChamberedMod.id("gun_hud"),
				GunHud::render
		);
	}

	private static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.gui.hud.isHidden()) {
			return;
		}

		ItemStack main = player.getMainHandItem();
		if (!(main.getItem() instanceof GunItem gunItem)) {
			return;
		}

		var font = client.font;
		int sw = client.getWindow().getGuiScaledWidth();
		int sh = client.getWindow().getGuiScaledHeight();

		GunState state = gunItem.getGunState(main);
		String line = formatAmmo(state);
		int ammoW = font.width(line);
		int ammoX = sw - ammoW - 8;
		int ammoY = sh - 22;
		graphics.fill(ammoX - 4, ammoY - 3, sw - 4, ammoY + 11, 0x66000000);
		graphics.text(font, line, ammoX, ammoY, 0xFFE8E0D0);

		if (DrawState.isDrawing()) {
			String draw = "Drawing…";
			int w = font.width(draw);
			graphics.text(font, draw, sw / 2 - w / 2, sh / 2 + 40, 0xFFAAAAAA);
		}
	}

	private static String formatAmmo(GunState state) {
		String chamber = state.isChambered() ? "1" : "0";
		if (state.magazine().isPresent()) {
			AmmoContents mag = state.magazine().get();
			return chamber + " | " + mag.count() + "/" + mag.capacity();
		}
		return chamber + " | —";
	}
}
