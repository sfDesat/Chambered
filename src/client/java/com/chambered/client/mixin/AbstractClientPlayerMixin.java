package com.chambered.client.mixin;

import com.chambered.client.feel.AdsState;
import com.chambered.item.GunItem;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies ADS FOV reduction while holding a Chambered gun.
 */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void chambered$adsFov(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		ItemStack main = self.getMainHandItem();
		if (!(main.getItem() instanceof GunItem)) {
			return;
		}
		float adsMul = AdsState.fovMultiplier();
		if (adsMul >= 0.999f) {
			return;
		}
		cir.setReturnValue(cir.getReturnValue() * adsMul);
	}
}
