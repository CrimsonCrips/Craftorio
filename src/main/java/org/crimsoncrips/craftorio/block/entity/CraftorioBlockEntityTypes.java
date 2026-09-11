package org.crimsoncrips.craftorio.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;


public class CraftorioBlockEntityTypes {

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craftorio.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SinkerBlockEntity>> SINKER = BLOCK_ENTITIES.register("sinker", () ->
			BlockEntityType.Builder.of(SinkerBlockEntity::new, CraftorioBlocks.SINKER.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoSinkerBlockEntity>> AUTO_SINKER = BLOCK_ENTITIES.register("auto_sinker", () ->
			BlockEntityType.Builder.of(AutoSinkerBlockEntity::new, CraftorioBlocks.AUTO_SINKER.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ValueCondenserBlockEntity>> VALUE_CONDENSER = BLOCK_ENTITIES.register("value_condenser", () ->
			BlockEntityType.Builder.of(ValueCondenserBlockEntity::new, CraftorioBlocks.VALUE_CONDENSER.get()).build(null));

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SINKER.get(), (be, side) -> new InvWrapper(be));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AUTO_SINKER.get(), (be, side) -> new InvWrapper(be));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, VALUE_CONDENSER.get(), (be, side) -> new InvWrapper(be));
	}

}