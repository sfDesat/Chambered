package com.chambered.client.render;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import com.chambered.item.GunItem;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.renderer.GeoItemRenderer;

import com.google.common.base.Suppliers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/**
 * Attaches GeckoLib item renderers on the client for every {@link GunItem}.
 * Model/animation/texture paths follow each item's registry id.
 */
public final class GunRenderers {
	private GunRenderers() {
	}

	public static void register() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof GunItem gunItem) {
				attach(gunItem);
			}
		}
	}

	private static void attach(GunItem item) {
		Supplier<GeoItemRenderer<GunItem>> renderer = Suppliers.memoize(() -> new GunItemRenderer(item));

		GeoRenderProvider provider = new GeoRenderProvider() {
			@Override
			public GeoItemRenderer<?> getGeoItemRenderer() {
				return renderer.get();
			}
		};

		injectRenderProvider(item, provider);
	}

	private static void injectRenderProvider(GunItem item, GeoRenderProvider provider) {
		try {
			AnimatableInstanceCache cache = item.getAnimatableInstanceCache();
			Field field = AnimatableInstanceCache.class.getDeclaredField("renderProvider");
			field.setAccessible(true);
			field.set(cache, (Supplier<GeoRenderProvider>) () -> provider);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to attach GeckoLib renderer for " + item, e);
		}
	}
}
