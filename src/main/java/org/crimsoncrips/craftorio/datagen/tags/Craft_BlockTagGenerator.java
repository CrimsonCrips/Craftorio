package org.crimsoncrips.craftorio.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.crimsoncrips.craftorio.Craftorio;


import java.util.concurrent.CompletableFuture;

public class Craft_BlockTagGenerator extends IntrinsicHolderTagsProvider<Block> {

	public static final TagKey<Block> COPPER = BlockTags.create(Craftorio.prefix("coral"));


	public Craft_BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
		super(output, Registries.BLOCK, future, block -> block.builtInRegistryHolder().key(), Craftorio.MODID, helper);
	}

	@Override
	public String getName() {
		return "Craftorio Block Tags";
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {


	}
}
