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
		this.addBlock(CraftorioBlocks.AUTO_SINKER,"Auto Sinker");
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
		this.addMisc("starter_contract_description", "A basic shipment to get your operation started.");
		this.addMisc("gave_points", "Gave %s points to %s");
		this.addMisc("effect_timer_enabled", "Effect timer display enabled");
		this.addMisc("effect_timer_disabled", "Effect timer display disabled");

		this.addMisc("hub_title", "Craftorio Menu");
		this.addMisc("hub_shop", "Item Shop");
		this.addMisc("reveal_contract_button", "Available Contracts");
		this.addMisc("owned_contracts_button", "My Contracts");

		this.addMisc("contract_reveal_title", "Shipment Contract");
		this.addMisc("no_contracts", "You have no active contracts.");
		this.addMisc("contract_time_remaining", "Time Remaining: %s");
		this.addMisc("contract_bounty", "Items Needed");
		this.addMisc("contract_item_rewards", "Item Rewards");
		this.addMisc("contract_point_reward", "Point Reward: %s");
		this.addMisc("contract_bounty_line", "%s / %s %s");
		this.addMisc("contract_reward_line", "%sx %s");
		this.addMisc("claim_contract_button", "Claim");
		this.addMisc("contract_claimed", "Claimed");
		this.addMisc("back", "Back");
		this.addMisc("contract_details_title", "Contract Details");
		this.addMisc("choose_a_contract", "Choose a Contract");
		this.addMisc("contract_punishment_line", "Failure Punishment: %s");
		this.addMisc("contract_punishment_received", "Contract \"%s\" failed! You have been punished with %s");
		this.addMisc("contract_abandoned", "[ABANDONED]");
		this.addMisc("abandon_contract_button", "Abandon");
		this.addMisc("abandon_contract_title", "Abandon Contract?");
		this.addMisc("abandon_contract_message", "Are you sure you want to abandon \"%s\"? This cannot be undone and may result in punishment.");
		this.addMisc("contract_refresh_timer", "Next refresh: %s");
		this.addMisc("welcome_toast_message", "Welcome to Craftorio! Press %s to open the Craftorio menu.");
		this.addMisc("new_contracts_toast", "New shipment contracts are available!");
		this.addMisc("skip_claim_animation_label", "Skip Claim Animation: %s");
		this.addMisc("force_contract_refresh_button", "Force Refresh (Creative)");
		this.addMisc("contract_refresh_time_set", "Contract refresh time set to %s seconds");
		this.addMisc("effect_timer_time_set", "Random effect timer set to %s seconds");

		this.addMisc("universal_based_label", "Universal Based: %s");
		this.addMisc("chunk_based_label", "Chunk Based: %s");
		this.addMisc("no_borders_based_label", "No Borders: %s");

		this.add("key.craftorio.open_hub", "Open Craftorio Menu");
		this.add("key.categories.craftorio", "Craftorio");

		this.add("advancements.craftorio.root.title", "Craftorio");
		this.add("advancements.craftorio.root.description", "The Points Shall Flow!");

		this.add("advancements.craftorio.millionaire.title", "Millionaire");
		this.add("advancements.craftorio.millionaire.description", "Make one million points");
		this.add("advancements.craftorio.the_human_body.title", "The Human Body");
		this.add("advancements.craftorio.the_human_body.description", "Make 7 octillion points");
		this.add("advancements.craftorio.russias_lawsuit.title", "Russia's Lawsuit");
		this.add("advancements.craftorio.russias_lawsuit.description", "Make 20 decillion points");
		this.add("advancements.craftorio.universal_number.title", "Universal Number");
		this.add("advancements.craftorio.universal_number.description", "Make 1 sesvigintillion points");
		this.add("advancements.craftorio.capture_of_the_true_overlord.title", "Capture of the True Overlord");
		this.add("advancements.craftorio.capture_of_the_true_overlord.description", "Make 1 trestrigintillion points");
		this.add("advancements.craftorio.the_miners_number.title", "The Miner's Number");
		this.add("advancements.craftorio.the_miners_number.description", "Make 1 centillion points");
		this.add("advancements.craftorio.existential_infinity.title", "Existential Infinity");
		this.add("advancements.craftorio.existential_infinity.description", "Reach Infinity");
		this.add("advancements.craftorio.existential_debt.title", "Existential Debt");
		this.add("advancements.craftorio.existential_debt.description", "Reach -Infinity");

		this.addItem(CraftorioItems.EFFECT_ITEM,"Effect Item");
		this.addItem(CraftorioItems.MYSTERY_EFFECT_ITEM,"Mystery Effect Item");
		this.addItem(CraftorioItems.CLAIM_ITEM,"Chunk Claim Item");

		this.addRegistryName("general_1", "General 1");
		this.addRegistryName("general_2", "General 2");
		this.addRegistryName("general_3", "General 3");
		this.addRegistryName("general_4", "General 4");
		this.addRegistryName("general_5", "General 5");

		this.addRegistryName("general_multiplier_1_5", "1.5 Multiplier");
		this.addRegistryName("general_multiplier_2", "2 Multiplier");
		this.addRegistryName("general_multiplier_3", "3 Multiplier");
		this.addRegistryName("general_multiplier_4", "4 Multiplier");
		this.addRegistryName("general_multiplier_5", "5 Multiplier");
		this.addRegistryName("general_multiplier_6", "6 Multiplier");
		this.addRegistryName("general_multiplier_7", "7 Multiplier");
		this.addRegistryName("general_multiplier_8", "8 Multiplier");
		this.addRegistryName("general_multiplier_9", "9 Multiplier");
		this.addRegistryName("general_multiplier_10", "10 Multiplier");

		this.addRegistryName("general_multiplier_neg_1_5", "-1.5 Multiplier");
		this.addRegistryName("general_multiplier_neg_2", "-2 Multiplier");
		this.addRegistryName("general_multiplier_neg_3", "-3 Multiplier");
		this.addRegistryName("general_multiplier_neg_4", "-4 Multiplier");
		this.addRegistryName("general_multiplier_neg_5", "-5 Multiplier");
		this.addRegistryName("general_multiplier_neg_6", "-6 Multiplier");
		this.addRegistryName("general_multiplier_neg_7", "-7 Multiplier");
		this.addRegistryName("general_multiplier_neg_8", "-8 Multiplier");
		this.addRegistryName("general_multiplier_neg_9", "-9 Multiplier");
		this.addRegistryName("general_multiplier_neg_10", "-10 Multiplier");

		this.addRegistryName("0_25_increase", "0.25 Increase");
		this.addRegistryName("0_25_decrease", "0.25 Decrease");

		this.addRegistryName("copper_block_buff", "Copper Block Buff");
		this.addRegistryName("copper_block_debuff", "Copper Block Debuff");
	}
}
