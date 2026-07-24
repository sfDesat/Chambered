package com.chambered.client.feel;

/**
 * Client recoil / recovery state.
 */
public final class RecoilState {
	private float pitch;
	private float yaw;

	public float pitch() {
		return pitch;
	}

	public float yaw() {
		return yaw;
	}

	public void addImpulse(float pitchDelta, float yawDelta) {
		this.pitch += pitchDelta;
		this.yaw += yawDelta;
	}

	public void tick(float recovery) {
		this.pitch *= recovery;
		this.yaw *= recovery;
	}
}
