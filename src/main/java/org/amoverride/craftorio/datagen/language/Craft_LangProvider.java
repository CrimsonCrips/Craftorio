package org.amoverride.craftorio.datagen.language;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class Craft_LangProvider extends LanguageProvider {

	public Craft_LangProvider(PackOutput output, String id, String locale) {
		super(output, id, locale);
	}

	public void addMisc(String subtitleKey,String text) {
		this.add("misc.craftorio." + subtitleKey,text);
	}


}
