package com.chambered.client;

import com.chambered.client.input.ChamberedKeybinds;
import com.chambered.client.render.BulletEntityRenderer;
import com.chambered.client.render.GunRenderers;
import com.chambered.client.ui.AttachScreens;
import com.chambered.registry.ModEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class ChamberedModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityTypes.BULLET, BulletEntityRenderer::new);
		GunRenderers.register();
		ChamberedKeybinds.register();
		AttachScreens.register();
	}
}
