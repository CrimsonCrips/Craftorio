package org.crimsoncrips.craftorio.datagen.recipe;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public abstract class CraftorioRecipeHelper extends RecipeProvider {

	public CraftorioRecipeHelper(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider);
	}


}
