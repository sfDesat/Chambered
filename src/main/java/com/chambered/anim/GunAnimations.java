package com.chambered.anim;

import com.chambered.item.GunItem;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Per-gun GeckoLib clip names + controller registration.
 * Clip ids are {@code animation.<namespace>.<path>.<clip>} so each gun's
 * {@code geckolib/animations/item/<path>.animation.json} stays independent.
 */
public final class GunAnimations {
	public static final String CONTROLLER = "main";

	public static final String CLIP_IDLE = "idle";
	public static final String CLIP_FIRE = "fire";
	public static final String CLIP_BOLT = "bolt_cycle";
	public static final String CLIP_RELOAD = "reload";
	public static final String CLIP_EMPTY = "empty";
	public static final String CLIP_INSPECT = "inspect";
	public static final String CLIP_DRAW = "draw";

	private GunAnimations() {
	}

	public static String clip(Identifier gunId, String clip) {
		return "animation." + gunId.getNamespace() + "." + gunId.getPath() + "." + clip;
	}

	public static String idle(Identifier gunId) {
		return clip(gunId, CLIP_IDLE);
	}

	public static String fire(Identifier gunId) {
		return clip(gunId, CLIP_FIRE);
	}

	public static String bolt(Identifier gunId) {
		return clip(gunId, CLIP_BOLT);
	}

	public static String reload(Identifier gunId) {
		return clip(gunId, CLIP_RELOAD);
	}

	public static String empty(Identifier gunId) {
		return clip(gunId, CLIP_EMPTY);
	}

	public static String inspect(Identifier gunId) {
		return clip(gunId, CLIP_INSPECT);
	}

	public static String draw(Identifier gunId) {
		return clip(gunId, CLIP_DRAW);
	}

	public static void registerControllers(GunItem item, AnimatableManager.ControllerRegistrar controllers) {
		Identifier gunId = BuiltInRegistries.ITEM.getKey(item);
		RawAnimation idleAnim = RawAnimation.begin().thenLoop(idle(gunId));
		RawAnimation fireAnim = RawAnimation.begin().thenPlay(fire(gunId));
		RawAnimation boltAnim = RawAnimation.begin().thenPlay(bolt(gunId));
		RawAnimation reloadAnim = RawAnimation.begin().thenPlay(reload(gunId));
		RawAnimation emptyAnim = RawAnimation.begin().thenPlay(empty(gunId));
		RawAnimation inspectAnim = RawAnimation.begin().thenPlay(inspect(gunId));
		RawAnimation drawAnim = RawAnimation.begin().thenPlay(draw(gunId));

		controllers.add(
				new AnimationController<GunItem>(CONTROLLER, 0, state -> {
					AnimationController<GunItem> controller = state.controller();
					if (controller.isPlayingTriggeredAnimation()) {
						return PlayState.CONTINUE;
					}
					if (!state.isCurrentAnimation(idleAnim)) {
						return state.setAndContinue(idleAnim);
					}
					return PlayState.CONTINUE;
				})
						.triggerableAnim(CLIP_FIRE, fireAnim)
						.triggerableAnim("bolt", boltAnim)
						.triggerableAnim(CLIP_RELOAD, reloadAnim)
						.triggerableAnim(CLIP_EMPTY, emptyAnim)
						.triggerableAnim(CLIP_INSPECT, inspectAnim)
						.triggerableAnim(CLIP_DRAW, drawAnim)
		);
	}
}
