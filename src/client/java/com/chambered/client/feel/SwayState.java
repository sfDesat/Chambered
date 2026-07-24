package com.chambered.client.feel;

/**
 * Client idle / movement sway.
 */
public final class SwayState {
	private float pitch;
	private float yaw;

	public float pitch() {
		return pitch;
	}

	public float yaw() {
		return yaw;
	}

	public void set(float pitch, float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}
}
