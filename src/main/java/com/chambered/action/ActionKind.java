package com.chambered.action;

/**
 * Timed server actions that complete later (reload now; jam clear later).
 */
public enum ActionKind {
	RELOAD,
	/** Reserved for Phase 4. */
	JAM_CLEAR
}
