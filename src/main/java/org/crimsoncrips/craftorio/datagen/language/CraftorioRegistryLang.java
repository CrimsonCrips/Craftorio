package org.crimsoncrips.craftorio.datagen.language;

public final class CraftorioRegistryLang {

	private CraftorioRegistryLang() {}

	public static void addTranslations(CraftLangGen lang) {
		addUpgrades(lang);
		addContracts(lang);
		addEffects(lang);
	}

	private static void addUpgrades(CraftLangGen lang) {
		lang.addUpgradeLang("root", "Craftorio", "Adds +0.1 to your multiplier");
		lang.addUpgradeLang("mult_1", "Multiplier I", "Adds +0.25 to your multiplier");
		lang.addUpgradeLang("mult_2", "Multiplier II", "Adds +0.5 to your multiplier per level");
		lang.addUpgradeLang("mult_3", "Multiplier III", "Adds +1 to your multiplier per level");
		lang.addUpgradeLang("mult_4", "Multiplier IV", "Adds +2.5 to your multiplier per level");
		lang.addUpgradeLang("difficulty_multiplier", "Risk Premium", "Adds to your multiplier based on the world difficulty: +0.5 on Easy, +1 on Normal, +2.5 on Hard, nothing on Peaceful");
		lang.addUpgradeLang("mult_amplifier_1", "Amplifier I", "Increases your total multiplier bonus by 50%");
		lang.addUpgradeLang("mult_amplifier_2", "Amplifier II", "Increases your total multiplier bonus by a further 100%");
		lang.addUpgradeLang("overdrive", "Overdrive", "Increases your total multiplier bonus by a further 200%");
		lang.addUpgradeLang("base_value_1", "Appraisal I", "Adds 10 to every item's base value per level");
		lang.addUpgradeLang("base_value_2", "Appraisal II", "Adds 100 to every item's base value per level");
		lang.addUpgradeLang("base_value_3", "Appraisal III", "Adds 1,000 to every item's base value per level");
		lang.addUpgradeLang("value_surge_1", "Value Surge I", "Increases item base value by 50%");
		lang.addUpgradeLang("value_surge_2", "Value Surge II", "Increases item base value by a further 100%");
		lang.addUpgradeLang("manual_sink_value", "Hands-On", "Items sunk by hand through the Sinker are worth 25% more per level");
		lang.addUpgradeLang("condenser_cap", "Condenser Capacity", "Condensed items can hold 100% more value per level");
		lang.addUpgradeLang("condenser_vault", "Condenser Vault", "Condensed items can hold an extra 1 quadrillion value");
		lang.addUpgradeLang("contract_refresh_1", "Quick Turnaround", "Contract offers refresh 60 seconds sooner per level");
		lang.addUpgradeLang("contract_refresh_2", "Express Contracts", "Contract offers refresh 25% faster per level");
		lang.addUpgradeLang("rarer_contract_1", "Discerning Clients", "Raises the chance of rarer contracts by 5% per level");
		lang.addUpgradeLang("rarer_contract_2", "Exotic Clients", "Raises the chance of rarer contracts by 50% per level");
		lang.addUpgradeLang("refresh_cost", "Haggling", "Manual contract refreshes get cheaper with each level, up to 43% off");
		lang.addUpgradeLang("punishment_duration", "Leniency", "Punishments last a third shorter, and half as long at max level");
		lang.addUpgradeLang("contract_momentum", "Contract Momentum", "Each level adds a 20% chance to gain the Contract Momentum effect whenever you complete a contract");
		lang.addUpgradeLang("effect_timer_1", "Fortune's Pace", "Random effects arrive 60 seconds sooner per level");
		lang.addUpgradeLang("effect_timer_display_unlock", "Effect Foresight", "Shows the time until your next random effect in the Active Effects screen");
		lang.addUpgradeLang("effect_timer_2", "Fortune's Rush", "Random effects arrive 25% faster per level");
		lang.addUpgradeLang("rarer_effect_1", "Lucky Draw", "Raises the chance of rarer random effects by 5% per level");
		lang.addUpgradeLang("rarer_effect_2", "Jackpot", "Raises the chance of rarer random effects by 50% per level");
		lang.addUpgradeLang("effect_duration", "Lingering Fortune", "Effects last 25% longer per level");
		lang.addUpgradeLang("wake_up_productive", "Rise and Grind", "Each level adds a 20% chance to gain the Productive effect whenever you wake up and skip the night");
		lang.addUpgradeLang("trade_economic_boom", "Merchant's Favor", "Each level adds a 20% chance to gain the Economic Boom effect whenever you trade with a villager");
		lang.addUpgradeLang("spoils_of_war", "Spoils of War", "Each level adds a 25% chance to gain the Spoils of War effect whenever you win a raid");
		lang.addUpgradeLang("expansion_cost", "Surveyor", "Land expansion gets cheaper with each level, down to half price");
		lang.addUpgradeLang("shop_cost", "Bulk Buyer", "Shop prices get cheaper with each level, up to 43% off");

		lang.addUpgradeLang("rebirth_root", "Second Wind", "Adds +1 to your multiplier in every life");
		lang.addUpgradeLang("double_or_nothing_unlock", "Double Or Nothing", "Unlock the double or nothing feature in the sinker, allowing you to double the points of items");
		lang.addUpgradeLang("bet_bonus", "High Roller", "Double or Nothing pays out +0.25x more per level");
		lang.addUpgradeLang("bet_odds", "Loaded Dice", "Raises your Double or Nothing win chance by 3% per level");
		lang.addUpgradeLang("lost_bet_refund", "Insurance", "Refunds 10% of a lost bet per level");
		lang.addUpgradeLang("effect_rune_shop_unlock", "Rune Forging", "Unlocks the ability to buy Effect Runes and Mystery Effect Runes with a chosen number of random effects, each extra effect steeply multiplies the price");
		lang.addUpgradeLang("lucky_star", "Lucky Star", "Random effects arrive 50% faster in every life");
		lang.addUpgradeLang("rebirth_rarer_effect", "Fated", "Raises the chance of rarer random effects by 25% per level, in every life");
		lang.addUpgradeLang("standing_clientele", "Standing Clientele", "Contract offers refresh 50% faster in every life");
		lang.addUpgradeLang("rebirth_rarer_contract", "Renowned Name", "Raises the chance of rarer contracts by 25% per level, in every life");
		lang.addUpgradeLang("echoes", "Echoes of Past Lives", "Increases your total multiplier bonus by 25% per level, in every life");
		lang.addUpgradeLang("inherited_appraisal", "Inherited Appraisal", "Increases item base value by 50% per level, in every life");
		lang.addUpgradeLang("sink_value_scaling", "Practice Makes Perfect", "Items gain bonus base value the more times you have sunk them");
		lang.addUpgradeLang("land_grant", "Land Grant", "Land expansion gets cheaper with each level, down to a third of the price");
		lang.addUpgradeLang("vitality", "Vitality", "+1 heart per level");
		lang.addUpgradeLang("fortitude", "Fortitude", "+2 armor per level");
		lang.addUpgradeLang("might", "Might", "+1 attack damage per level");
		lang.addUpgradeLang("swiftness", "Swiftness", "+5% movement speed per level");
		lang.addUpgradeLang("long_reach", "Long Reach", "+0.5 block reach per level");

		lang.addUpgradeLang("build_blitz", "Build Blitz", "While holding a placed schematic, press the Build Blitz key to launch blocks from your inventory into the build, filling up to 20% (configurable) of its missing blocks. Recharges over 2 hours (configurable), and blocks that fail to land are returned and refund their share of the recharge");
		lang.addUpgradeLang("build_blitz_reload", "Rapid Reload", "The Build Blitz recharges 30 minutes faster per level");
		lang.addUpgradeLang("build_blitz_coverage", "Wider Blitz", "The Build Blitz fills 15% more of a build's missing blocks per level");
		lang.addUpgradeLang("advancement_multiplier", "Legacy of Deeds", "Completed advancements raise your multiplier, including the ones you have already earned");
		lang.addUpgradeLang("eternal_flame", "Eternal Flame", "Increases your total multiplier bonus by 200%, forever");
		lang.addUpgradeLang("contract_completion_scaling", "Renown", "Every contract you complete in a life adds to your multiplier");
		lang.addUpgradeLang("renown_boost", "Legend", "Each completed contract adds +0.01 more to your multiplier");
		lang.addUpgradeLang("sacred_appraisal", "Sacred Appraisal", "Increases item base value by 200%, forever");
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
