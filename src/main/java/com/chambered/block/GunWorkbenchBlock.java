package com.chambered.block;

import com.mojang.serialization.MapCodec;

import com.chambered.menu.WorkbenchMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Field / camp workbench used primarily to repair firearms.
 */
public class GunWorkbenchBlock extends Block {
	public static final MapCodec<GunWorkbenchBlock> CODEC = simpleCodec(GunWorkbenchBlock::new);

	/** Matches {@code gun_workbench.json}: top, legs, shelf, and vise. */
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(0.0, 12.0, 0.0, 16.0, 15.0, 16.0),
			Block.box(1.0, 0.0, 1.0, 3.0, 12.0, 3.0),
			Block.box(13.0, 0.0, 1.0, 15.0, 12.0, 3.0),
			Block.box(1.0, 0.0, 13.0, 3.0, 12.0, 15.0),
			Block.box(13.0, 0.0, 13.0, 15.0, 12.0, 15.0),
			Block.box(3.0, 4.0, 3.0, 13.0, 5.0, 13.0),
			Block.box(9.0, 15.0, 9.0, 14.0, 16.0, 14.0),
			Block.box(10.0, 16.0, 10.0, 13.0, 18.0, 13.0)
	);

	public GunWorkbenchBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			player.openMenu(state.getMenuProvider(level, pos));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider(
				(containerId, inventory, player) -> new WorkbenchMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
				Component.translatable("container.chambered.gun_workbench")
		);
	}
}
