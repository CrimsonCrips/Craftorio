package org.amoverride.craftorio.datagen.language;

import net.minecraft.data.PackOutput;
import org.amoverride.craftorio.Craftorio;

public class Craft_LangGen extends Craft_LangProvider {

	public Craft_LangGen(PackOutput output) {
		super(output, Craftorio.MODID,"en_us");
	}


	protected void addTranslations() {

		this.addMisc("claim_land", "Claim Land");
		this.addMisc("unclaim_land", "Unclaim Land");


	}
}
