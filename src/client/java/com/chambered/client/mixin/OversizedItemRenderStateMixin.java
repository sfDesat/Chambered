package com.chambered.client.mixin;

import com.chambered.client.ui.FieldAttachPreview;

import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Matches PIP 3D scale to the expanded field-attach gun bounds.
 */
@Mixin(OversizedItemRenderState.class)
public class OversizedItemRenderStateMixin {
	@Inject(method = "scale", at = @At("HEAD"), cancellable = true)
	private void chambered$hiResFieldAttachGunScale(CallbackInfoReturnable<Float> cir) {
		OversizedItemRenderState self = (OversizedItemRenderState) (Object) this;
		if (FieldAttachPreview.isHiResGunState(self.guiItemRenderState())) {
			cir.setReturnValue(16.0F * FieldAttachPreview.GUN_SCALE);
		}
	}
}
