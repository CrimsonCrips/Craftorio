package org.crimsoncrips.craftorio.datagen.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;


import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CraftorioRecipeGenerator extends CraftorioRecipeHelper {

	public CraftorioRecipeGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider);
	}


	@Override
	protected void buildRecipes(RecipeOutput recipeOutput) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CraftorioBlocks.SINKER.asItem())
				.requires(Ingredient.of(Blocks.BARREL.asItem()))
				.requires(Ingredient.of(Items.GOLD_INGOT))
				.save(recipeOutput);
	}
}
