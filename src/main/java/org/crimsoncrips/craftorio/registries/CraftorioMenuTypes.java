package org.crimsoncrips.craftorio.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.inventory.AutoSinkerMenu;
import org.crimsoncrips.craftorio.inventory.AutoValueCondenserMenu;
import org.crimsoncrips.craftorio.inventory.ContractCreatorMenu;
import org.crimsoncrips.craftorio.inventory.SinkerMenu;
import org.crimsoncrips.craftorio.inventory.ValueCondenserMenu;

public class CraftorioMenuTypes {

	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, Craftorio.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<SinkerMenu>> SINKER = CONTAINERS.register("sinker_menu",
		() -> new MenuType<>(SinkerMenu::sinkerMenu, FeatureFlags.REGISTRY.allFlags()));

	public static final DeferredHolder<MenuType<?>, MenuType<AutoSinkerMenu>> AUTO_SINKER = CONTAINERS.register("auto_sinker_menu",
		() -> new MenuType<>(AutoSinkerMenu::clientMenu, FeatureFlags.REGISTRY.allFlags()));

	public static final DeferredHolder<MenuType<?>, MenuType<ValueCondenserMenu>> VALUE_CONDENSER = CONTAINERS.register("value_condenser_menu",
		() -> new MenuType<>(ValueCondenserMenu::clientMenu, FeatureFlags.REGISTRY.allFlags()));

	public static final DeferredHolder<MenuType<?>, MenuType<AutoValueCondenserMenu>> AUTO_VALUE_CONDENSER = CONTAINERS.register("auto_value_condenser_menu",
		() -> new MenuType<>(AutoValueCondenserMenu::clientMenu, FeatureFlags.REGISTRY.allFlags()));

	public static final DeferredHolder<MenuType<?>, MenuType<ContractCreatorMenu>> CONTRACT_CREATOR = CONTAINERS.register("contract_creator_menu",
		() -> new MenuType<>(ContractCreatorMenu::contractCreatorMenu, FeatureFlags.REGISTRY.allFlags()));

}