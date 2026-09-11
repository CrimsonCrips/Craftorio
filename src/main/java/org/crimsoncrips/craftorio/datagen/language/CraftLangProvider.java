package org.crimsoncrips.craftorio.datagen.language;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class CraftLangProvider extends LanguageProvider {

	public CraftLangProvider(PackOutput output, String id, String locale) {
		super(output, id, locale);
	}

	public void addMisc(String subtitleKey,String text) {
		this.add("misc.craftorio." + subtitleKey,text);
	}

	public void addContractLang(String subtitleKey,String title,String description) {
		this.add("registry." + subtitleKey + ".title",title);
		this.add("registry." + subtitleKey + ".description",description);
	}

	public void addRegistryName(String subtitleKey,String title) {
		this.add("registry." + subtitleKey,title);
	}


}
