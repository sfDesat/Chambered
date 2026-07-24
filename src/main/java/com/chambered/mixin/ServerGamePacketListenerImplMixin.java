package com.chambered.mixin;

import com.chambered.item.GunItem;

import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks F-key main/offhand swap when either hand holds a Chambered gun.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
	private void chambered$blockGunOffhandSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
		if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
			return;
		}
		ItemStack main = this.player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = this.player.getItemInHand(InteractionHand.OFF_HAND);
		if (main.getItem() instanceof GunItem || off.getItem() instanceof GunItem) {
			ci.cancel();
		}
	}
}
