package org.crimsoncrips.craftorio.datagen.language;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.crimsoncrips.craftorio.Craftorio;

public class CraftLangGen extends LanguageProvider {

	public CraftLangGen(PackOutput output) {
		super(output, Craftorio.MODID,"en_us");
	}

	public void addMisc(String subtitleKey,String text) {
		this.add("misc.craftorio." + subtitleKey,text);
	}

	public void addContractLang(String subtitleKey,String title,String description) {
		this.add("registry." + subtitleKey + ".title",title);
		this.add("registry." + subtitleKey + ".description",description);
	}

	public void addUpgradeLang(String subtitleKey,String title,String description) {
		this.addUpgradeLang(Craftorio.MODID,subtitleKey,title,description);
	}

	public void addUpgradeLang(String modId,String subtitleKey,String title,String description) {
		this.add("misc." + modId + ".upgrade_" + subtitleKey,title);
		this.add("misc." + modId + ".upgrade_" + subtitleKey + "_description",description);
	}

	public void addRegistryName(String subtitleKey,String title) {
		this.add("registry." + subtitleKey,title);
	}

	protected void addTranslations() {
		CraftorioGeneralLang.addTranslations(this);
		CraftorioRegistryLang.addTranslations(this);
	}
}
