package com.chambered.registry;

import java.util.function.UnaryOperator;

import com.chambered.ChamberedMod;
import com.chambered.component.AmmoContents;
import com.chambered.component.GunState;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModComponents {
	public static final DataComponentType<GunState> GUN_STATE = register(
			"gun_state",
			builder -> builder.persistent(GunState.CODEC).networkSynchronized(GunState.STREAM_CODEC)
	);

	public static final DataComponentType<AmmoContents> AMMO_CONTENTS = register(
			"ammo_contents",
			builder -> builder.persistent(AmmoContents.CODEC).networkSynchronized(AmmoContents.STREAM_CODEC)
	);

	private ModComponents() {
	}

	private static <T> DataComponentType<T> register(String path, UnaryOperator<DataComponentType.Builder<T>> operator) {
		return Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				ChamberedMod.id(path),
				operator.apply(DataComponentType.builder()).build()
		);
	}

	public static void register() {
		ChamberedMod.LOGGER.info("Registered data components");
	}
}
