package com.chambered.registry;

import com.chambered.ChamberedMod;
import com.chambered.attach.FieldAttachData;
import com.chambered.menu.FieldAttachMenu;
import com.chambered.menu.WorkbenchMenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
	public static final ExtendedMenuType<FieldAttachMenu, FieldAttachData> FIELD_ATTACH = Registry.register(
			BuiltInRegistries.MENU,
			ChamberedMod.id("field_attach"),
			new ExtendedMenuType<>(FieldAttachMenu::new, FieldAttachData.STREAM_CODEC)
	);

	public static final MenuType<WorkbenchMenu> GUN_WORKBENCH = register(
			"gun_workbench",
			WorkbenchMenu::new
	);

	private ModMenuTypes() {
	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> factory) {
		return Registry.register(
				BuiltInRegistries.MENU,
				ChamberedMod.id(name),
				new MenuType<>(factory, FeatureFlags.VANILLA_SET)
		);
	}

	public static void register() {
		ChamberedMod.LOGGER.info("Registered menu types");
	}
}
