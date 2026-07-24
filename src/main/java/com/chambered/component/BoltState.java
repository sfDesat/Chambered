package com.chambered.component;

import com.mojang.serialization.Codec;

/**
 * Bolt / slide position on a firearm.
 * Expanded behavior (cycling timing, jam clear) comes in later phases.
 */
public enum BoltState {
	FORWARD,
	LOCKED_BACK,
	CYCLING;

	public static final Codec<BoltState> CODEC = Codec.STRING.xmap(
			name -> BoltState.valueOf(name.toUpperCase()),
			state -> state.name().toLowerCase()
	);
}
