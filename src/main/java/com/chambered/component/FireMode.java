package com.chambered.component;

import com.mojang.serialization.Codec;

/**
 * Selectable fire mode. Guns declare which modes they allow in definition data (Phase 1).
 */
public enum FireMode {
	SEMI,
	BURST,
	AUTO;

	public static final Codec<FireMode> CODEC = Codec.STRING.xmap(
			name -> FireMode.valueOf(name.toUpperCase()),
			mode -> mode.name().toLowerCase()
	);
}
