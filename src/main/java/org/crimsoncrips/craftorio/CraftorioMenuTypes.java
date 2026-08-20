package org.crimsoncrips.craftorio;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.StonecutterMenu;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;

public class CraftorioMenuTypes {

	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, Craftorio.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<SinkerMenu>> SINKER = CONTAINERS.register("sinker_menu",
		() -> new MenuType<>(SinkerMenu::sinkerMenu, FeatureFlags.REGISTRY.allFlags()));

}