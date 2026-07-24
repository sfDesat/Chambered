package com.chambered.action;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.chambered.component.AmmoContents;
import com.chambered.component.BoltState;
import com.chambered.component.GunState;
import com.chambered.item.GunItem;
import com.chambered.registry.ModComponents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative timed gun actions. State mutates on {@link #complete}, not at begin.
 * Cancels if the player stops holding the same gun item type, dies, or disconnects.
 */
public final class ActionTimeline {
	private static final Map<UUID, PendingAction> PENDING = new ConcurrentHashMap<>();

	private ActionTimeline() {
	}

	public static boolean isBusy(ServerPlayer player) {
		return PENDING.containsKey(player.getUUID());
	}

	public static Optional<ActionKind> currentKind(ServerPlayer player) {
		PendingAction pending = PENDING.get(player.getUUID());
		return pending == null ? Optional.empty() : Optional.of(pending.kind());
	}

	public static boolean begin(ServerPlayer player, PendingAction action) {
		if (PENDING.containsKey(player.getUUID())) {
			return false;
		}
		PENDING.put(player.getUUID(), action);
		return true;
	}

	public static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		PendingAction pending = PENDING.get(player.getUUID());
		if (pending == null) {
			return;
		}
		if (!pending.stillValid(player)) {
			cancel(player);
			return;
		}
		if (player.level().getGameTime() >= pending.completeAtTick()) {
			PENDING.remove(player.getUUID());
			pending.complete(player);
		}
	}

	public static void cancel(ServerPlayer player) {
		PendingAction pending = PENDING.remove(player.getUUID());
		if (pending != null) {
			pending.cancel(player);
		}
	}

	public static void clear(ServerPlayer player) {
		cancel(player);
	}

	/**
	 * Mag swap applied when the timeline completes. Mag is reserved from inventory at begin.
	 */
	public record PendingReload(
			ActionKind kind,
			long completeAtTick,
			Identifier gunId,
			Optional<AmmoContents> ejectedMagazine,
			Optional<Identifier> ejectedMagazineItem,
			Optional<AmmoContents> insertMagazine,
			Optional<Identifier> insertMagazineItem,
			ItemStack reservedInsertStack
	) implements PendingAction {
		public PendingReload(
				long completeAtTick,
				Identifier gunId,
				Optional<AmmoContents> ejectedMagazine,
				Optional<Identifier> ejectedMagazineItem,
				Optional<AmmoContents> insertMagazine,
				Optional<Identifier> insertMagazineItem,
				ItemStack reservedInsertStack
		) {
			this(
					ActionKind.RELOAD,
					completeAtTick,
					gunId,
					ejectedMagazine,
					ejectedMagazineItem,
					insertMagazine,
					insertMagazineItem,
					reservedInsertStack
			);
		}

		@Override
		public boolean stillValid(ServerPlayer player) {
			ItemStack main = player.getMainHandItem();
			if (!(main.getItem() instanceof GunItem)) {
				return false;
			}
			return BuiltInRegistries.ITEM.getKey(main.getItem()).equals(gunId);
		}

		@Override
		public void complete(ServerPlayer player) {
			ItemStack gunStack = player.getMainHandItem();
			if (!(gunStack.getItem() instanceof GunItem gunItem) || !stillValid(player)) {
				cancel(player);
				return;
			}

			GunState state = gunItem.getGunState(gunStack);
			// Drop seated mag first (should still match plan; do not trust mid-action edits).
			state = state.withoutMagazine();

			if (ejectedMagazine.isPresent() && ejectedMagazineItem.isPresent()) {
				ItemStack ejected = createMagazineStack(ejectedMagazine.get(), ejectedMagazineItem.get());
				if (!ejected.isEmpty() && !player.getInventory().add(ejected)) {
					player.drop(ejected, false);
				}
			}

			if (insertMagazine.isPresent() && insertMagazineItem.isPresent()) {
				AmmoContents contents = insertMagazine.get();
				state = state.withMagazine(contents, insertMagazineItem.get());

				boolean wasLocked = state.bolt() == BoltState.LOCKED_BACK;
				if (!state.isChambered() && !contents.isEmpty()) {
					AmmoContents afterFeed = contents.withCount(contents.count() - 1);
					state = state
							.withChamber(Optional.of(contents.ammoType()))
							.withBolt(BoltState.FORWARD)
							.withMagazine(afterFeed);
				} else if (wasLocked && state.isChambered()) {
					state = state.withBolt(BoltState.FORWARD);
				}
			}

			gunItem.setGunState(gunStack, state);
		}

		@Override
		public void cancel(ServerPlayer player) {
			if (!reservedInsertStack.isEmpty()) {
				if (!player.getInventory().add(reservedInsertStack.copy())) {
					player.drop(reservedInsertStack.copy(), false);
				}
			}
		}

		private static ItemStack createMagazineStack(AmmoContents contents, Identifier magId) {
			if (!BuiltInRegistries.ITEM.containsKey(magId)) {
				return ItemStack.EMPTY;
			}
			ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(magId));
			stack.set(ModComponents.AMMO_CONTENTS, contents);
			return stack;
		}
	}

	public interface PendingAction {
		ActionKind kind();

		long completeAtTick();

		boolean stillValid(ServerPlayer player);

		void complete(ServerPlayer player);

		void cancel(ServerPlayer player);
	}
}
