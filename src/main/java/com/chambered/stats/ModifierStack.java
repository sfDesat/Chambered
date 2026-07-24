package com.chambered.stats;

import java.util.Map;

import com.chambered.attach.SlotType;
import com.chambered.component.GunState;
import com.chambered.data.GunDefinitions;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Collects modifiers from attachments / condition / future systems, then resolves
 * {@link ResolvedGunStats} from a gun's base definition.
 *
 * <p>ADS is applied at resolve-time (not stored on the stack) so hipfire and ADS
 * share one code path.
 */
public final class ModifierStack {
	private StatModifier combined = StatModifier.IDENTITY;

	public ModifierStack() {
	}

	public ModifierStack add(StatModifier modifier) {
		combined = combined.combine(modifier);
		return this;
	}

	/** Applies each seated attachment's definition modifiers. */
	public ModifierStack addAttachments(Map<SlotType, Identifier> attachments) {
		for (Identifier id : attachments.values()) {
			GunDefinitions.attachment(id).ifPresent(def -> add(def.modifiers()));
		}
		return this;
	}

	/**
	 * Condition 100 = pristine. Lower condition gently worsens sway / recovery (Phase 4 expands this).
	 */
	public ModifierStack addCondition(float condition) {
		float t = 1.0f - Mth.clamp(condition, 0.0f, 100.0f) / 100.0f;
		if (t <= 0.0f) {
			return this;
		}
		return add(new StatModifier(
				1.0f, 0.0f,
				1.0f, 0.0f,
				1.0f + t * 0.25f, 0.0f,
				0.0f,
				1.0f, 1.0f,
				0.0f,
				1.0f + t * 0.1f, 1.0f + t * 0.1f,
				1.0f - t * 0.15f,
				1.0f,
				1.0f, 1.0f
		));
	}

	public StatModifier combined() {
		return combined;
	}

	/**
	 * Build a stack from gun state (attachments + condition). ADS is passed to {@link #resolve}.
	 */
	public static ModifierStack fromGunState(GunState state) {
		return new ModifierStack()
				.addAttachments(state.attachments())
				.addCondition(state.condition());
	}
}
