package org.crimsoncrips.craftorio.datagen.language;

public final class CraftorioRegistryLang {

	private CraftorioRegistryLang() {}

	public static void addTranslations(CraftLangGen lang) {
		addUpgrades(lang);
		addContracts(lang);
		addEffects(lang);
	}

	private static void addUpgrades(CraftLangGen lang) {
		lang.addUpgradeLang("bet_bonus_1", "Bet Bonus 1", "Increase bet bonus by 10%");
		lang.addUpgradeLang("bet_bonus_2", "Bet Bonus 2", "Increase bet bonus by 30%");
		lang.addUpgradeLang("bet_bonus_3", "Bet Bonus 3", "Increase bet bonus by 90%");
		lang.addUpgradeLang("lost_bet_refund_1", "Refund Bet 1", "Refunds 10% of betted value if lost");
		lang.addUpgradeLang("lost_bet_refund_2", "Refund Bet 2", "Refunds 30% of betted value if lost");
		lang.addUpgradeLang("root", "Craftorio", "Adds 0.1 to mult");
		lang.addUpgradeLang("contract_refresh_1", "Contract Refresh 1", "Speeds up the contract refresh timer by 110%");
		lang.addUpgradeLang("contract_refresh_2", "Contract Refresh 2", "Speeds up the contract refresh timer by 130%");
		lang.addUpgradeLang("contract_refresh_3", "Contract Refresh 3", "Speeds up the contract refresh timer by 150%");
		lang.addUpgradeLang("double_or_nothing_unlock", "Double Or Nothing", "Unlock the double or nothing feature in the sinker, allowing you to double the points of items");
		lang.addUpgradeLang("build_blitz", "Build Blitz", "While holding a placed schematic, press the Build Blitz key to launch blocks from your inventory into the build, filling up to 20% (configurable) of its missing blocks. Recharges over 40 minutes (configurable), and blocks that fail to land are returned and refund their share of the recharge");
		lang.addUpgradeLang("build_blitz_coverage", "Wider Blitz", "The Build Blitz fills 10% more of a build's missing blocks per level");
		lang.addUpgradeLang("build_blitz_reload", "Rapid Reload", "The Build Blitz recharges 5 minutes faster per level");
		lang.addUpgradeLang("effect_rune_shop_unlock", "Rune Forging", "Unlocks the ability to buy Effect Runes and Mystery Effect Runes with a chosen number of random effects, each extra effect steeply multiplies the price");
		lang.addUpgradeLang("effect_timer_display_unlock", "Effect Foresight", "Shows the time until your next random effect in the Active Effects screen");
		lang.addUpgradeLang("effect_timer_1", "Effect Timer Speed 1", "Reduces the effect timer by 1 second");
		lang.addUpgradeLang("effect_timer_2", "Effect Timer Speed 2", "Reduces the effect timer by 1.4 seconds");
		lang.addUpgradeLang("effect_timer_3", "Effect Timer Speed 3", "Reduces the effect timer by 2.4 seconds");
		lang.addUpgradeLang("effect_timer_4", "Effect Timer Speed 4", "Reduces the effect timer by 8 seconds");
		lang.addUpgradeLang("expansion_cost_1", "Expansion Reduction 1", "Reduces expansion cost by 10%");
		lang.addUpgradeLang("expansion_cost_2", "Expansion Reduction 2", "Reduces expansion cost by 30%");
		lang.addUpgradeLang("mult_1", "Add Multiplier 1", "Adds 0.2 to mult");
		lang.addUpgradeLang("mult_2", "Add Multiplier 2", "Adds 0.5 to mult");
		lang.addUpgradeLang("mult_3", "Add Multiplier 3", "Adds 0.8 to mult");
		lang.addUpgradeLang("mult_4", "Add Multiplier 4", "Adds 1.0 to mult");
		lang.addUpgradeLang("mult_5", "Add Multiplier 5", "Adds 2.0 to mult");
		lang.addUpgradeLang("mult_6", "Multiply Multiplier", "Multiplies mult by 2x");
		lang.addUpgradeLang("punishment_duration_1", "Punishment Duration", "Reduce punishment duration by 25%");
		lang.addUpgradeLang("rarer_contract_1", "Rarer Contract Chance 1", "Get rarer contracts by 2%");
		lang.addUpgradeLang("rarer_contract_2", "Rarer Contract Chance 2", "Get rarer contracts by 5%");
		lang.addUpgradeLang("rarer_contract_3", "Rarer Contract Chance 3", "Get rarer contracts by 10%");
		lang.addUpgradeLang("rarer_contract_4", "Rarer Contract Chance 4", "Get rarer contracts by 50%");
		lang.addUpgradeLang("rarer_contract_5", "Rarer Contract Chance 5", "Get rarer contracts by 100%");
		lang.addUpgradeLang("rarer_contract_6", "Rarer Contract Chance 6", "Multiply rarer contract chance by x3");
		lang.addUpgradeLang("rarer_effect_1", "Rarer Effects", "Increases rarer effects by 2%");
		lang.addUpgradeLang("rarer_effect_2", "Rarer Effects", "Increases rarer effects by 5%");
		lang.addUpgradeLang("rarer_effect_3", "Rarer Effects", "Increases rarer effects by 10%");
		lang.addUpgradeLang("rarer_effect_4", "Rarer Effects", "Increases rarer effects by 50%");
		lang.addUpgradeLang("rarer_effect_5", "Rarer Effects", "Increases rarer effects by 100%");
		lang.addUpgradeLang("refresh_cost_1", "Refresh Cost", "Contract refresh cost reduced by 12%");
		lang.addUpgradeLang("refresh_cost_2", "Refresh Cost", "Contract refresh cost reduced by 28%");
		lang.addUpgradeLang("base_value_1", "Add Base Value", "Adds 10 to item base value");
		lang.addUpgradeLang("base_value_2", "Add Base Value", "Adds 20 to item base value");
		lang.addUpgradeLang("base_value_3", "Add Base Value", "Adds 25 to item base value");
		lang.addUpgradeLang("base_value_4", "Add Base Value", "Adds 50 to item base value");
		lang.addUpgradeLang("base_value_5", "Add Base Value", "Adds 120 to item base value");
		lang.addUpgradeLang("base_value_6", "Add Base Value", "Adds 130 to item base value");
		lang.addUpgradeLang("base_value_7", "Add Base Value", "Adds 135 to item base value");
		lang.addUpgradeLang("base_value_8", "Mult Base Value", "Multiplies item base value by 2");
		lang.addUpgradeLang("manual_sink_value_1", "Manual Sink Bonus", "Increases the value of items sunk manually via the Sinker by 25%");
		lang.addUpgradeLang("wake_up_productive", "Rise and Grind", "Each level adds a 20% chance to gain the Productive effect whenever you wake up and refresh the day");
		lang.addUpgradeLang("condenser_cap_1", "Condenser Capacity I", "Doubles the value a condensed item can hold");
		lang.addUpgradeLang("condenser_cap_2", "Condenser Capacity II", "Adds +400% to the value a condensed item can hold");
		lang.addUpgradeLang("condenser_cap_3", "Condenser Capacity III", "Adds 100 trillion to the value a condensed item can hold");
		lang.addUpgradeLang("difficulty_multiplier", "Risk Premium", "Adds to your multiplier based on the world difficulty: +0.5 on Easy, +1.0 on Normal, +2.5 on Hard, nothing on Peaceful");
		lang.addUpgradeLang("contract_momentum", "Contract Momentum", "Each level adds a 25% chance to gain the Contract Momentum effect (x2.5 points for 5 minutes) whenever you complete a contract");
		lang.addUpgradeLang("spoils_of_war", "Spoils of War", "Each level adds a 25% chance to gain the Spoils of War effect (x6 points for 20 minutes) whenever you win a raid");
		lang.addUpgradeLang("trade_economic_boom", "Merchant's Favor", "Each level adds a 20% chance to gain the Economic Boom effect whenever you trade with a villager");
		lang.addUpgradeLang("effect_duration_1", "Effect Duration", "Add effect duration by 50%");
		lang.addUpgradeLang("effect_duration_2", "Effect Duration", "Add effect duration by 100%");

	}

	private static void addContracts(CraftLangGen lang) {
		lang.addContractLang("gold_throne_construction", "Gold Throne Construction","A local king requires the materials for a gold throne");
		lang.addContractLang("animal_feed", "Animal Feeds","Requesting some feeds for my animals, I'll bake a nice cake for anyone willing to fullfill");
		lang.addContractLang("brewing_materials", "Brewing Materials","Ran out of supplies for my brewing, Bring me some fresh equipment and materials");
		lang.addContractLang("dyeabolical", "Dyeabolical","I lIke cOLOrs, gIve cOlor!!!");
		lang.addContractLang("archery_resupply", "Archery Resupply","We've been attacked by a horde of zombies, we need to refresh our gear");
		lang.addContractLang("terraforming", "Terraforming","We need to excavate a cave for our underground base");
		lang.addContractLang("care_package", "Care Package","Need a care package for new players on my minecraft server");
		lang.addContractLang("cake_delivery", "Cake Delivery","Mom said I couldn't have cake, i want cake");
		lang.addContractLang("kingdoms_feast", "Kingdom's Feast", "The king is holding a feast for a very special occasion. You are to procure its meals");
		lang.addContractLang("chicken_coop", "Chicken Coop", "I want a simple chicken coop for my lovely chickens <3");
		lang.addContractLang("witchy_business", "Witchy Business", "The local witch is in needing of a new home");
		lang.addContractLang("wasted_reimains", "Wasted Reimains", "No Comment");
		lang.addContractLang("inquisitors_burning", "Inquisitor's Burning", "The Inquisitor needs materials for his weekly witch burning.");
		lang.addContractLang("copernicium", "Copernicium", "We're in need of any copper materials for an ongoing project.");
		lang.addContractLang("trophy_headed", "Trophy Headed", "I want the head of a variety of mobs out there in the wild for my collection");
		lang.addContractLang("multiversal_collection", "Multiversal Collection", "I am Trazyn, I require a piece of everything of your world for my collection.");
		lang.addContractLang("monster_annihilator", "Monster Annihilator", "To prove your strength, You are to slay and retrieve the remains of the hostiles of this world");
		lang.addContractLang("executed_escapee", "Executed Escapee", "I'm in need of some tools to help me cheat death tonight.");
		lang.addContractLang("villager_house", "Villager's Retreat", "Raise a simple plains village house, just as the villagers build them.");
		lang.addContractLang("end_ship", "Vessel of the End", "Reconstruct an End City ship, purpur and all.");
		lang.addContractLang("matterial_calculation", "Matterial Calculation", "An entity named MattBattWings, wishes to test you with your tenacity by making a simple replica of his iconic computer");
		lang.addContractLang("nether_beacon", "Wealth Determiner", "You are to construct a full netherite beacon to prove your worth");
		lang.addContractLang("mumbo_10_door", "Mumbo's 10th Door", "Hello player, this is Mumbo, and I want to test you on your redstone skills!");
		lang.addContractLang("create_steam_engine", "Creating an Engine", "Create a Steam Engine");
	}


	private static void addEffects(CraftLangGen lang) {
		lang.addRegistryName("kingdom_tariff", "Kingdom's Tariff");
		lang.addRegistryName("inquisitors_wrath", "Inquisitor's Wrath");
		lang.addRegistryName("copper_deficiency", "Copper Deficiency");
		lang.addRegistryName("trazyns_curse", "Trazyn's Curse");
		lang.addRegistryName("commeupance_of_the_gods", "Commeupance of the Gods");
		lang.addRegistryName("economic_boom", "Economic Boom");
		lang.addRegistryName("lucky", "Lucky!");
		lang.addRegistryName("contract_momentum", "Contract Momentum");
		lang.addRegistryName("spoils_of_war", "Spoils of War");
		lang.addRegistryName("strait_to_deficits", "Strait To Deficits");
		lang.addRegistryName("productive", "Productive");
		lang.addRegistryName("redstone_mania", "Redstone Mania");
		lang.addRegistryName("music_fest", "Music Fest");
		lang.addRegistryName("archery_season", "Archery Season");
		lang.addRegistryName("universal_demand", "Universal Demand");
		lang.addRegistryName("black_holdover", "Black Holdover");
		lang.addRegistryName("monopolized", "Monopolized");
		lang.addRegistryName("oversupplied", "Oversupplied");
		lang.addRegistryName("dense_traffic", "Dense Traffic");
		lang.addRegistryName("rugpulled", "Rugpulled");
		lang.addRegistryName("inflated_valuables", "Inflated Valuables");
		lang.addRegistryName("witch_curse", "Witch's Curse");
		lang.addRegistryName("reiminder", "A Simple Reiminder");

	}
}
