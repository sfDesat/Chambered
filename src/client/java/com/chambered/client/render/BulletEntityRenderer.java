package com.chambered.client.render;

import com.chambered.entity.BulletEntity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Invisible stub renderer. Tracers / meshes come later.
 */
public class BulletEntityRenderer extends EntityRenderer<BulletEntity, EntityRenderState> {
	public BulletEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}
}
