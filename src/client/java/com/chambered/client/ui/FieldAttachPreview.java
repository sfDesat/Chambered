package com.chambered.client.ui;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;

/**
 * True while the field-attach screen is open so FIXED gun renders can face
 * barrel-left and bone projection can share that yaw.
 *
 * <p>Also tracks the hi-res gun preview submit so PIP render scale matches the
 * expanded on-screen bounds (avoids nearest-neighbor upscale blur).
 */
public final class FieldAttachPreview {
	/** On-screen multiplier vs a normal 16×16 GUI item; keep in sync with bone projection. */
	public static final float GUN_SCALE = 6.5f;

	private static final Set<GuiItemRenderState> HI_RES_GUN_STATES =
			Collections.newSetFromMap(new WeakHashMap<>());

	private static boolean markNextGunItem;

	private FieldAttachPreview() {
	}

	public static boolean isActive() {
		Minecraft client = Minecraft.getInstance();
		return client.gui != null && client.gui.screen() instanceof AttachScreens.FieldAttachScreen;
	}

	/** Call immediately before submitting the field-attach gun {@link GuiItemRenderState}. */
	public static void markNextItemHiResGun() {
		markNextGunItem = true;
	}

	public static boolean consumeHiResGunMark() {
		if (!markNextGunItem) {
			return false;
		}
		markNextGunItem = false;
		return true;
	}

	public static void rememberHiResGunState(GuiItemRenderState state) {
		HI_RES_GUN_STATES.add(state);
	}

	public static boolean isHiResGunState(GuiItemRenderState state) {
		return HI_RES_GUN_STATES.contains(state);
	}
}
