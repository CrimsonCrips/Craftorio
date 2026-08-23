package org.crimsoncrips.craftorio.datagen.language;

import net.minecraft.data.PackOutput;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;

public class CraftLangGen extends CraftLangProvider {

	public CraftLangGen(PackOutput output) {
		super(output, Craftorio.MODID,"en_us");
	}


	protected void addTranslations() {
		this.addBlock(CraftorioBlocks.SINKER,"Sinker");
		this.addMisc("claim_land", "Claim Land");
		this.addMisc("unclaim_land", "Unclaim Land");


	}
}
