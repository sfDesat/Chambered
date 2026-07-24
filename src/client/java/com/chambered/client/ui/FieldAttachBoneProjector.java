package com.chambered.client.ui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.chambered.attach.SlotType;
import com.chambered.item.GunItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

/**
 * Projects GeckoLib {@code attach_*} bone pivots into field-menu screen space so
 * connector lines track real mount points per gun model.
 */
public final class FieldAttachBoneProjector {
	/**
	 * Model units → GUI item pixels (before {@link FieldAttachScreen}'s gun scale).
	 * Tuned so makarov muzzle/optic anchors sit on the preview silhouette.
	 */
	private static final float MODEL_TO_ITEM = 1.0f;

	/** Extra yaw applied in {@link com.chambered.client.render.GunItemRenderer} during attach preview. */
	public static final float PREVIEW_YAW_DEGREES = 180.0f;

	private static final Map<Identifier, Map<String, float[]>> PIVOT_CACHE = new ConcurrentHashMap<>();

	private FieldAttachBoneProjector() {
	}

	public static void invalidateCache() {
		PIVOT_CACHE.clear();
	}

	/**
	 * @param gunScale same scale used when drawing the FIXED preview
	 * @return screen-space point, or empty if the bone is missing
	 */
	public static Optional<Vec2> projectMount(
			ItemStack gunStack,
			SlotType slot,
			int centerX,
			int centerY,
			float gunScale
	) {
		if (!(gunStack.getItem() instanceof GunItem gunItem)) {
			return Optional.empty();
		}
		return pivot(gunItem.getDefinitionId(), slot.boneName()).map(pivot -> {
			float[] screen = modelToItemPixels(pivot[0], pivot[1], pivot[2]);
			float sx = centerX + screen[0] * gunScale;
			float sy = centerY + screen[1] * gunScale;
			return new Vec2(sx, sy);
		});
	}

	/**
	 * Converts a geo bone pivot into item-local pixels relative to the item center,
	 * matching the FIXED preview + {@link #PREVIEW_YAW_DEGREES} yaw.
	 *
	 * <p>Blockbench/GeckoLib: +Y up, Makarov muzzle toward −Z.
	 * After preview yaw, gun length lies along Z → screen X; Y flips for GUI space.
	 */
	private static float[] modelToItemPixels(float x, float y, float z) {
		double yaw = Math.toRadians(PREVIEW_YAW_DEGREES);
		double cos = Math.cos(yaw);
		double sin = Math.sin(yaw);
		double ry = y;
		double rz = -x * sin + z * cos;

		float itemX = (float) (-rz * MODEL_TO_ITEM);
		float itemY = (float) (-ry * MODEL_TO_ITEM);
		return new float[] {itemX, itemY};
	}

	private static Optional<float[]> pivot(Identifier gunId, String boneName) {
		Map<String, float[]> bones = PIVOT_CACHE.computeIfAbsent(gunId, FieldAttachBoneProjector::loadPivots);
		float[] pivot = bones.get(boneName);
		return pivot == null ? Optional.empty() : Optional.of(pivot);
	}

	private static Map<String, float[]> loadPivots(Identifier gunId) {
		Map<String, float[]> map = new ConcurrentHashMap<>();
		Identifier geoPath = Identifier.fromNamespaceAndPath(
				gunId.getNamespace(),
				"geckolib/models/item/" + gunId.getPath() + ".geo.json"
		);
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(geoPath);
		if (resource.isEmpty()) {
			return map;
		}
		try (InputStream in = resource.get().open();
				InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
			if (geometries == null || geometries.isEmpty()) {
				return map;
			}
			JsonObject geometry = geometries.get(0).getAsJsonObject();
			JsonArray bones = geometry.getAsJsonArray("bones");
			if (bones == null) {
				return map;
			}
			for (JsonElement element : bones) {
				JsonObject bone = element.getAsJsonObject();
				if (!bone.has("name") || !bone.has("pivot")) {
					continue;
				}
				String name = bone.get("name").getAsString();
				JsonArray pivot = bone.getAsJsonArray("pivot");
				if (pivot.size() < 3) {
					continue;
				}
				map.put(name, new float[] {
						pivot.get(0).getAsFloat(),
						pivot.get(1).getAsFloat(),
						pivot.get(2).getAsFloat()
				});
			}
		} catch (Exception ignored) {
			map.clear();
		}
		return map;
	}
}
