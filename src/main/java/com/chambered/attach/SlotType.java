package com.chambered.attach;

import net.minecraft.util.StringRepresentable;

/**
 * Attachment slot kinds on a firearm rail / mount layout.
 * Field-menu layout assumes barrel faces <strong>left</strong> on screen.
 */
public enum SlotType implements StringRepresentable {
	MUZZLE,
	OPTIC,
	UNDERBARREL,
	STOCK,
	SIDE;

	@Override
	public String getSerializedName() {
		return name().toLowerCase();
	}

	/** GeckoLib bone name for this mount on gun models. */
	public String boneName() {
		return "attach_" + getSerializedName();
	}

	/**
	 * Floating field-menu slot offset from the on-screen gun center (pixels).
	 * Barrel-left: muzzle left, stock right. Compact enough for large GUI scales.
	 */
	public int menuOffsetX() {
		return switch (this) {
			case MUZZLE -> -110;
			case STOCK -> 110;
			case SIDE -> -78;
			case OPTIC, UNDERBARREL -> 0;
		};
	}

	public int menuOffsetY() {
		return switch (this) {
			case OPTIC -> -58;
			case UNDERBARREL -> 52;
			case SIDE -> 36;
			case MUZZLE -> -2;
			case STOCK -> 2;
		};
	}

	/** Connector anchor on the gun silhouette (barrel left). */
	public int mountPointX() {
		return switch (this) {
			case MUZZLE -> -40;
			case STOCK -> 36;
			case SIDE -> -18;
			case OPTIC, UNDERBARREL -> -2;
		};
	}

	public int mountPointY() {
		return switch (this) {
			case OPTIC -> -18;
			case UNDERBARREL -> 14;
			case SIDE -> 6;
			case MUZZLE -> -2;
			case STOCK -> 4;
		};
	}
}
