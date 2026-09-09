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

		this.addMisc("sinker_button", "Sink Points");
		this.addMisc("points_required", "Points Required");
		this.addMisc("expand_border", "Expand Border");
		this.addMisc("too_much_value", "Value exceeds Infinity!");
		this.addMisc("advancement_value", "Points given for advancement : ");

		this.addMisc("search", "Search");
		this.addMisc("quantity", "Quantity");
		this.addMisc("max_minus", "Max-");
		this.addMisc("max_plus", "Max+");
		this.addMisc("buy", "Buy");
		this.addMisc("cancel", "Cancel");
		this.addMisc("done", "Done");
		this.addMisc("enter_valid_quantity", "Enter a valid quantity");

		this.addMisc("shop_title", "Shop");
		this.addMisc("shop_disabled", "The shop is currently disabled.");
		this.addMisc("item_not_sold", "That item isn't sold in the shop.");
		this.addMisc("not_enough_points", "You don't have enough points for that.");
		this.addMisc("already_own_chunk", "You already own that chunk.");
		this.addMisc("claim_item_tooltip", "Right-click a block to claim its chunk for free.");
		this.addMisc("purchased_items", "Purchased %sx %s.");
		this.addMisc("purchase_title", "Purchase %s");
		this.addMisc("claim_shop_title", "Buy Claim Chunk Items");
		this.addMisc("points_each_suffix", " points each");
		this.addMisc("points_total_suffix", " points total");
		this.addMisc("points_suffix", " points");

		this.addMisc("points_label", "Points : ");
		this.addMisc("next_effect_in", "Next effect in: ");
		this.addMisc("locked_suffix", " (Locked)");

		this.addMisc("value_browser_title", "Item Values");
		this.addMisc("sorted_most_valuable", "Sorted: Most Valuable");
		this.addMisc("sorted_most_valueless", "Sorted: Most Valueless");

		this.addMisc("config_title", "Craftorio Config");
		this.addMisc("server_config_title", "Craftorio Server Config");
		this.addMisc("server_config_button", "Server Config");
		this.addMisc("point_formatting_label", "Point Formatting: %s");
		this.addMisc("format_raw", "Raw Numbers");
		this.addMisc("format_scientific", "Scientific");
		this.addMisc("format_short_suffix", "Short Suffix");
		this.addMisc("format_worded", "Worded");

		this.addMisc("contract_info", "Name:%s  Time:%s");
		this.addMisc("gave_points", "Gave %s points to %s");
		this.addMisc("effect_timer_enabled", "Effect timer display enabled");
		this.addMisc("effect_timer_disabled", "Effect timer display disabled");

		this.addMisc("hub_title", "Craftorio Menu");
		this.addMisc("hub_shop", "Item Shop");

		this.add("key.craftorio.open_hub", "Open Craftorio Menu");
		this.add("key.categories.craftorio", "Craftorio");

		this.add("advancements.craftorio.root.title", "Craftorio");
		this.add("advancements.craftorio.root.description", "Obtain a Sinker");
		this.add("advancements.craftorio.claim_land.title", "Land Baron");
		this.add("advancements.craftorio.claim_land.description", "Obtain a Chunk Claim Item");

		this.addItem(CraftorioItems.EFFECT_ITEM,"Effect Item");
		this.addItem(CraftorioItems.MYSTERY_EFFECT_ITEM,"Mystery Effect Item");
		this.addItem(CraftorioItems.CLAIM_ITEM,"Chunk Claim Item");

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
