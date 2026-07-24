package com.chambered.client.input;

import com.chambered.ChamberedMod;
import com.chambered.anim.GunAnimations;
import com.chambered.client.feel.AdsState;
import com.chambered.client.feel.ClientActionBusy;
import com.chambered.client.feel.ClientGunFeel;
import com.chambered.client.feel.DrawState;
import com.chambered.client.ui.GunDebugOverlay;
import com.chambered.client.ui.GunHud;
import com.chambered.component.BoltState;
import com.chambered.component.FireMode;
import com.chambered.component.GunState;
import com.chambered.data.GunDefinitions;
import com.chambered.item.GunItem;
import com.chambered.network.ServerboundChangeFireModePayload;
import com.chambered.network.ServerboundFirePayload;
import com.chambered.network.ServerboundOpenFieldAttachPayload;
import com.chambered.network.ServerboundReloadPayload;
import com.chambered.stats.ResolvedGunStats;

import com.geckolib.animatable.GeoItem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

/**
 * Fire, reload, fire-mode, ADS (use), inspect, field attach, and debug overlay.
 */
public final class ChamberedKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(ChamberedMod.id("chambered"));

	public static KeyMapping RELOAD;
	public static KeyMapping FIRE_MODE;
	public static KeyMapping INSPECT;
	public static KeyMapping FIELD_ATTACH;
	public static KeyMapping DEBUG_OVERLAY;

	/** Rising-edge tracker for semi / burst (one attempt per LMB press). */
	private static boolean attackWasDown;

	private ChamberedKeybinds() {
	}

	public static void register() {
		RELOAD = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chambered.reload",
				GLFW.GLFW_KEY_R,
				CATEGORY
		));
		FIRE_MODE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chambered.fire_mode",
				GLFW.GLFW_KEY_B,
				CATEGORY
		));
		INSPECT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chambered.inspect",
				GLFW.GLFW_KEY_I,
				CATEGORY
		));
		FIELD_ATTACH = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chambered.field_attach",
				GLFW.GLFW_KEY_V,
				CATEGORY
		));
		DEBUG_OVERLAY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chambered.debug_overlay",
				GLFW.GLFW_KEY_F8,
				CATEGORY
		));

		ClientPreAttackCallback.EVENT.register((client, player, clickCount) ->
				player != null && isHoldingGun(player.getMainHandItem())
		);

		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
				isHoldingGun(player.getMainHandItem()) ? InteractionResult.FAIL : InteractionResult.PASS
		);
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
				isHoldingGun(player.getMainHandItem()) ? InteractionResult.FAIL : InteractionResult.PASS
		);

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (!isHoldingGun(player.getMainHandItem())) {
				return InteractionResult.PASS;
			}
			// Allow opening the gun workbench while armed.
			var state = level.getBlockState(hitResult.getBlockPos());
			if (state.is(com.chambered.registry.ModBlocks.GUN_WORKBENCH)) {
				return InteractionResult.PASS;
			}
			return InteractionResult.FAIL;
		});
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
				isHoldingGun(player.getMainHandItem()) ? InteractionResult.FAIL : InteractionResult.PASS
		);
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (isHoldingGun(player.getItemInHand(hand))) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(ChamberedKeybinds::onEndTick);
		GunDebugOverlay.register();
		GunHud.register();
	}

	private static void onEndTick(Minecraft client) {
		ClientActionBusy.tick();

		if (client.player == null) {
			attackWasDown = false;
			AdsState.reset();
			ClientGunFeel.tick(client, false);
			DrawState.tick(client, false);
			return;
		}

		// Screen open (chat, inventory, …): can't hold ADS — keep ticking so aim eases out.
		if (client.gui.screen() != null) {
			attackWasDown = false;
			AdsState.setWantingAds(false);
			boolean holdingGun = isHoldingGun(client.player.getMainHandItem());
			AdsState.tick(client, holdingGun);
			ClientGunFeel.tick(client, false);
			DrawState.tick(client, false);
			return;
		}

		while (DEBUG_OVERLAY.consumeClick()) {
			GunDebugOverlay.toggle();
		}

		ItemStack main = client.player.getMainHandItem();
		boolean holdingGun = isHoldingGun(main);

		DrawState.tick(client, holdingGun);
		if (holdingGun && DrawState.consumeDrawAnim() && main.getItem() instanceof GunItem gunItem) {
			gunItem.triggerAnim(client.player, GeoItem.getId(main), GunAnimations.CONTROLLER, GunAnimations.CLIP_DRAW);
		}

		if (!holdingGun) {
			attackWasDown = false;
			AdsState.setWantingAds(false);
			AdsState.tick(client, false);
			ClientGunFeel.tick(client, false);
			ClientActionBusy.reset();
			return;
		}

		boolean adsDown = client.options.keyUse.isDown();
		AdsState.setWantingAds(adsDown);
		AdsState.tick(client, true);

		while (RELOAD.consumeClick()) {
			if (!ClientActionBusy.isBusy() && ClientPlayNetworking.canSend(ServerboundReloadPayload.TYPE)
					&& main.getItem() instanceof GunItem gunItem) {
				int reloadTicks = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(main.getItem()))
						.map(def -> ResolvedGunStats.of(def, gunItem.getGunState(main), 0.0f).reloadTicks())
						.orElse(20);
				ClientPlayNetworking.send(new ServerboundReloadPayload());
				ClientActionBusy.begin(reloadTicks);
			}
		}
		while (FIRE_MODE.consumeClick()) {
			if (ClientPlayNetworking.canSend(ServerboundChangeFireModePayload.TYPE)) {
				ClientPlayNetworking.send(ServerboundChangeFireModePayload.cycle());
			}
		}
		while (INSPECT.consumeClick()) {
			if (main.getItem() instanceof GunItem gunItem) {
				long id = GeoItem.getId(main);
				gunItem.triggerAnim(client.player, id, GunAnimations.CONTROLLER, "inspect");
			}
		}
		while (FIELD_ATTACH.consumeClick()) {
			if (ClientPlayNetworking.canSend(ServerboundOpenFieldAttachPayload.TYPE)) {
				ClientPlayNetworking.send(new ServerboundOpenFieldAttachPayload());
			}
		}

		boolean attackDown = client.options.keyAttack.isDown();
		GunState state = ((GunItem) main.getItem()).getGunState(main);
		FireMode mode = state.fireMode();

		if (attackDown && DrawState.canFire(client) && !ClientActionBusy.isBusy()) {
			boolean risingEdge = !attackWasDown;
			boolean shouldAttempt = switch (mode) {
				case SEMI, BURST -> risingEdge;
				case AUTO -> true;
			};
			if (shouldAttempt) {
				tryFire(client, main, state, risingEdge);
			}
		}
		attackWasDown = attackDown;

		ClientGunFeel.tick(client, true);
	}

	private static void tryFire(Minecraft client, ItemStack main, GunState state, boolean risingEdge) {
		if (!ClientPlayNetworking.canSend(ServerboundFirePayload.TYPE)) {
			return;
		}

		boolean canShoot = state.isChambered() && state.bolt() != BoltState.LOCKED_BACK;
		int interval = 3;
		if (main.getItem() instanceof GunItem gunItem) {
			interval = GunDefinitions.gun(BuiltInRegistries.ITEM.getKey(main.getItem()))
					.map(def -> ResolvedGunStats.of(def, gunItem.getGunState(main), AdsState.progress()).fireIntervalTicks())
					.orElse(3);
		}
		boolean aiming = AdsState.isAiming();

		if (canShoot) {
			if (!ClientGunFeel.tryAcquireShot(client, interval)) {
				return;
			}
			ClientPlayNetworking.send(new ServerboundFirePayload(aiming));
			ClientGunFeel.onFired(client);
			return;
		}

		if (risingEdge) {
			ClientPlayNetworking.send(new ServerboundFirePayload(aiming));
		}
	}

	public static boolean isHoldingGun(ItemStack stack) {
		return stack.getItem() instanceof GunItem;
	}
}
