package com.chambered.item;

import java.util.Optional;
import java.util.function.Consumer;

import com.chambered.anim.GunAnimations;
import com.chambered.component.AmmoContents;
import com.chambered.component.GunState;
import com.chambered.registry.ModComponents;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jetbrains.annotations.Nullable;

/**
 * Base firearm item. Definition id matches the item registry id.
 * GeckoLib renderer is attached from the client (Loom split cannot reference GeoRenderProvider here).
 * Guns are main-hand only — offhand placement is rejected.
 */
public class GunItem extends Item implements GeoItem {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public GunItem(Properties properties) {
		super(properties);
		GeoItem.registerSyncedAnimatable(this);
	}

	public Identifier getDefinitionId() {
		return BuiltInRegistries.ITEM.getKey(this);
	}

	public GunState getGunState(ItemStack stack) {
		return stack.getOrDefault(ModComponents.GUN_STATE, GunState.DEFAULT);
	}

	public void setGunState(ItemStack stack, GunState state) {
		stack.set(ModComponents.GUN_STATE, state);
	}

	/**
	 * Firing / reloading rewrite {@link GunState} every shot. Vanilla treats any
	 * component change as a re-equip and dips the held item — cancel that.
	 */
	@Override
	public boolean allowComponentsUpdateAnimation(
			Player player,
			InteractionHand hand,
			ItemStack oldStack,
			ItemStack newStack
	) {
		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		if (slot != EquipmentSlot.OFFHAND || !(owner instanceof Player player)) {
			return;
		}
		ItemStack off = player.getOffhandItem();
		if (!(off.getItem() instanceof GunItem)) {
			return;
		}
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		if (!player.getInventory().add(off)) {
			player.drop(off, false);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		GunAnimations.registerControllers(this, controllers);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			TooltipContext context,
			TooltipDisplay display,
			Consumer<Component> consumer,
			TooltipFlag flag
	) {
		GunState state = getGunState(stack);
		consumer.accept(Component.literal("Bolt: " + state.bolt().name().toLowerCase()));
		consumer.accept(Component.literal("Mode: " + state.fireMode().name().toLowerCase()));
		consumer.accept(Component.literal("Chamber: " + (state.isChambered() ? "loaded" : "empty")));
		consumer.accept(Component.literal(String.format("Condition: %.0f%%", state.condition())));
		if (!state.attachments().isEmpty()) {
			consumer.accept(Component.literal("Attachments: " + state.attachments().size()));
		}
		Optional<AmmoContents> mag = state.magazine();
		if (mag.isPresent()) {
			AmmoContents c = mag.get();
			consumer.accept(Component.literal("Mag: " + c.count() + "/" + c.capacity()));
		} else {
			consumer.accept(Component.literal("Mag: none"));
		}
	}
}
