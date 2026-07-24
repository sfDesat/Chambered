package com.chambered.client.mixin;

import com.chambered.client.ui.FieldAttachPreview;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Expands the field-attach gun's oversized PIP bounds by {@link FieldAttachPreview#GUN_SCALE}
 * so the GPU texture matches on-screen size (no post-blit nearest upscale).
 */
@Mixin(GuiItemRenderState.class)
public abstract class GuiItemRenderStateMixin {
	@Shadow
	@Final
	@Mutable
	private @Nullable ScreenRectangle oversizedItemBounds;

	@Shadow
	@Final
	@Mutable
	private @Nullable ScreenRectangle bounds;

	@Shadow
	@Final
	private Matrix3x2f pose;

	@Shadow
	@Final
	private @Nullable ScreenRectangle scissorArea;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void chambered$expandFieldAttachGunBounds(
			Matrix3x2f pose,
			TrackingItemStackRenderState itemStackRenderState,
			int x,
			int y,
			@Nullable ScreenRectangle scissorArea,
			CallbackInfo ci
	) {
		if (!FieldAttachPreview.consumeHiResGunMark()) {
			return;
		}

		float scale = FieldAttachPreview.GUN_SCALE;
		ScreenRectangle base = this.oversizedItemBounds;
		if (base == null) {
			base = new ScreenRectangle(x, y, 16, 16);
		}

		float centerX = base.left() + base.width() / 2.0f;
		float centerY = base.top() + base.height() / 2.0f;
		int width = Math.max(1, Math.round(base.width() * scale));
		int height = Math.max(1, Math.round(base.height() * scale));
		int left = Math.round(centerX - width / 2.0f);
		int top = Math.round(centerY - height / 2.0f);

		this.oversizedItemBounds = new ScreenRectangle(left, top, width, height);
		ScreenRectangle transformed = this.oversizedItemBounds.transformMaxBounds(this.pose);
		this.bounds = this.scissorArea != null ? this.scissorArea.intersection(transformed) : transformed;

		FieldAttachPreview.rememberHiResGunState((GuiItemRenderState) (Object) this);
	}
}
