package com.chambered.client.feel;

/**
 * Client-side mirror of {@link com.chambered.action.ActionTimeline} for input gating.
 */
public final class ClientActionBusy {
	private static int busyTicksRemaining;

	private ClientActionBusy() {
	}

	public static void begin(int ticks) {
		busyTicksRemaining = Math.max(busyTicksRemaining, Math.max(1, ticks));
	}

	public static boolean isBusy() {
		return busyTicksRemaining > 0;
	}

	public static void tick() {
		if (busyTicksRemaining > 0) {
			busyTicksRemaining--;
		}
	}

	public static void reset() {
		busyTicksRemaining = 0;
	}
}
