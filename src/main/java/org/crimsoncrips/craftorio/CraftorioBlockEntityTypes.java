package org.crimsoncrips.craftorio;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.block.entity.SinkerBlockEntity;


public class CraftorioBlockEntityTypes {

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craftorio.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SinkerBlockEntity>> SINKER = BLOCK_ENTITIES.register("sinker", () ->
			BlockEntityType.Builder.of(SinkerBlockEntity::new, CraftorioBlocks.SINKER.get()).build(null));


}