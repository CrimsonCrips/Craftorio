package org.crimsoncrips.craftorio.datagen.language;

import net.minecraft.data.PackOutput;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.item.CraftorioItems;

public class CraftLangGen extends CraftLangProvider {

	public CraftLangGen(PackOutput output) {
		super(output, Craftorio.MODID,"en_us");
	}


	protected void addTranslations() {
		this.addBlock(CraftorioBlocks.SINKER,"Sinker");
		this.addMisc("claim_land", "Claim Land");
		this.addMisc("unclaim_land", "Unclaim Land");

		this.addMisc("sinker_button", "Sink Points");
		this.addMisc("points_required", "Points Required");
		this.addMisc("expand_border", "Expand Border");
		this.addMisc("too_much_value", "Value exceeds Infinity!");
		this.addMisc("advancement_value", "Points given for advancement : ");

		this.addItem(CraftorioItems.EFFECT_ITEM,"Effect Item");
		this.addItem(CraftorioItems.MYSTERY_EFFECT_ITEM,"Mystery Effect Item");

		this.addRegistryName("general_1", "General 1");
		this.addRegistryName("general_2", "General 2");
		this.addRegistryName("general_3", "General 3");
		this.addRegistryName("general_4", "General 4");
		this.addRegistryName("general_5", "General 5");

		this.addRegistryName("0_25_increase", "0.25 Increase");

		this.addRegistryName("copper_block_buff", "Copper Block Buff");
		this.addRegistryName("copper_block_debuff", "Copper Block Debuff");
	}
}
