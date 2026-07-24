package com.chambered.action;

import com.chambered.data.GunDefinition;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Plays gun sounds from definition ids (with safe fallbacks).
 */
public final class GunSounds {
	private GunSounds() {
	}

	public static void playFire(ServerPlayer player, GunDefinition def) {
		play(player, def.sounds().fire(), 0.55f, 1.35f);
	}

	public static void playEmpty(ServerPlayer player, GunDefinition def) {
		play(player, def.sounds().empty(), 0.35f, 1.6f);
	}

	public static void playReload(ServerPlayer player, GunDefinition def) {
		play(player, def.sounds().reload(), 0.5f, 1.4f);
	}

	public static void playFireMode(ServerPlayer player, GunDefinition def) {
		play(player, def.sounds().fireMode(), 0.4f, 0.8f);
	}

	private static void play(ServerPlayer player, Identifier soundId, float volume, float pitch) {
		SoundEvent sound = resolve(soundId);
		if (sound == null) {
			return;
		}
		player.level().playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				sound,
				SoundSource.PLAYERS,
				volume,
				pitch
		);
	}

	private static SoundEvent resolve(Identifier id) {
		return BuiltInRegistries.SOUND_EVENT.get(id).map(Holder::value).orElse(null);
	}
}
