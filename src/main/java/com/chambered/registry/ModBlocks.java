package com.chambered.registry;

import java.util.function.Function;

import com.chambered.ChamberedMod;
import com.chambered.block.GunWorkbenchBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
	public static final Block GUN_WORKBENCH = register(
			"gun_workbench",
			GunWorkbenchBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.WOOD)
					.strength(2.5f)
					.sound(SoundType.WOOD),
			true
	);

	private ModBlocks() {
	}

	private static Block register(
			String name,
			Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties settings,
			boolean registerItem
	) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ChamberedMod.id(name));
		Block block = factory.apply(settings.setId(blockKey));
		if (registerItem) {
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ChamberedMod.id(name));
			BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
			Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		}
		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	public static void register() {
		ChamberedMod.LOGGER.info("Registered blocks");
	}
}
