package com.chambered.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.chambered.ChamberedMod;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * In-memory maps of data-driven gun content loaded from {@code data/chambered/...}.
 */
public final class GunDefinitions {
	private static Map<Identifier, GunDefinition> guns = Map.of();
	private static Map<Identifier, MagazineDefinition> magazines = Map.of();
	private static Map<Identifier, CaliberDefinition> calibers = Map.of();
	private static Map<Identifier, AmmoTypeDefinition> ammoTypes = Map.of();
	private static Map<Identifier, AttachmentDefinition> attachments = Map.of();

	private GunDefinitions() {
	}

	public static void registerReloadListeners() {
		ResourceLoader serverData = ResourceLoader.get(PackType.SERVER_DATA);

		serverData.registerReloadListener(ChamberedMod.id("calibers"), new SimpleJsonResourceReloadListener<>(
				CaliberDefinition.CODEC,
				FileToIdConverter.json("calibers")
		) {
			@Override
			protected void apply(Map<Identifier, CaliberDefinition> preparations, ResourceManager resourceManager, ProfilerFiller profiler) {
				calibers = freeze(preparations);
				ChamberedMod.LOGGER.info("Loaded {} caliber definition(s)", calibers.size());
			}
		});

		serverData.registerReloadListener(ChamberedMod.id("ammo_types"), new SimpleJsonResourceReloadListener<>(
				AmmoTypeDefinition.CODEC,
				FileToIdConverter.json("ammo_types")
		) {
			@Override
			protected void apply(Map<Identifier, AmmoTypeDefinition> preparations, ResourceManager resourceManager, ProfilerFiller profiler) {
				ammoTypes = freeze(preparations);
				ChamberedMod.LOGGER.info("Loaded {} ammo type definition(s)", ammoTypes.size());
			}
		});

		serverData.registerReloadListener(ChamberedMod.id("magazines"), new SimpleJsonResourceReloadListener<>(
				MagazineDefinition.CODEC,
				FileToIdConverter.json("magazines")
		) {
			@Override
			protected void apply(Map<Identifier, MagazineDefinition> preparations, ResourceManager resourceManager, ProfilerFiller profiler) {
				magazines = freeze(preparations);
				ChamberedMod.LOGGER.info("Loaded {} magazine definition(s)", magazines.size());
			}
		});

		serverData.registerReloadListener(ChamberedMod.id("guns"), new SimpleJsonResourceReloadListener<>(
				GunDefinition.CODEC,
				FileToIdConverter.json("guns")
		) {
			@Override
			protected void apply(Map<Identifier, GunDefinition> preparations, ResourceManager resourceManager, ProfilerFiller profiler) {
				guns = freeze(preparations);
				ChamberedMod.LOGGER.info("Loaded {} gun definition(s)", guns.size());
			}
		});

		serverData.registerReloadListener(ChamberedMod.id("attachments"), new SimpleJsonResourceReloadListener<>(
				AttachmentDefinition.CODEC,
				FileToIdConverter.json("attachments")
		) {
			@Override
			protected void apply(Map<Identifier, AttachmentDefinition> preparations, ResourceManager resourceManager, ProfilerFiller profiler) {
				attachments = freeze(preparations);
				ChamberedMod.LOGGER.info("Loaded {} attachment definition(s)", attachments.size());
			}
		});
	}

	public static Optional<GunDefinition> gun(Identifier id) {
		return Optional.ofNullable(guns.get(id));
	}

	public static Optional<MagazineDefinition> magazine(Identifier id) {
		return Optional.ofNullable(magazines.get(id));
	}

	public static Optional<CaliberDefinition> caliber(Identifier id) {
		return Optional.ofNullable(calibers.get(id));
	}

	public static Optional<AmmoTypeDefinition> ammoType(Identifier id) {
		return Optional.ofNullable(ammoTypes.get(id));
	}

	public static Optional<AttachmentDefinition> attachment(Identifier id) {
		return Optional.ofNullable(attachments.get(id));
	}

	public static Map<Identifier, GunDefinition> guns() {
		return guns;
	}

	public static Map<Identifier, AttachmentDefinition> attachments() {
		return attachments;
	}

	private static <T> Map<Identifier, T> freeze(Map<Identifier, T> source) {
		return Collections.unmodifiableMap(new HashMap<>(source));
	}
}
