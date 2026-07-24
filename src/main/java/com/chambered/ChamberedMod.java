package com.chambered;

import com.chambered.action.ActionTimeline;
import com.chambered.action.EquipGates;
import com.chambered.action.GunCooldowns;
import com.chambered.data.GunDefinitions;
import com.chambered.registry.ModBlocks;
import com.chambered.registry.ModComponents;
import com.chambered.registry.ModEntityTypes;
import com.chambered.registry.ModItems;
import com.chambered.registry.ModMenuTypes;
import com.chambered.registry.ModNetworking;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChamberedMod implements ModInitializer {
	public static final String MOD_ID = "chambered";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModComponents.register();
		ModBlocks.register();
		ModItems.register();
		ModMenuTypes.register();
		ModEntityTypes.register();
		ModNetworking.register();
		GunDefinitions.registerReloadListeners();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			EquipGates.tick(server);
			ActionTimeline.tick(server);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			var player = handler.player;
			ActionTimeline.clear(player);
			GunCooldowns.clear(player);
			EquipGates.clear(player);
		});

		LOGGER.info("Chambered initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
