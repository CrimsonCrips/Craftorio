package org.crimsoncrips.craftorio.datagen.tags;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.CraftorioItems;


import java.util.concurrent.CompletableFuture;

public class CraftorioItemTagGen extends ItemTagsProvider {
	public static final TagKey<Item> COPPER  = create("copper");
	public static final TagKey<Item> CHISELED_COPPER = create("chiseled_copper");
	public static final TagKey<Item> COPPER_GRATE = create("copper_grate");
	public static final TagKey<Item> CUT_COPPER = create("cut_copper");
	public static final TagKey<Item> CUT_COPPER_STAIRS = create("cut_copper_stairs");
	public static final TagKey<Item> CUT_COPPER_SLAB = create("cut_copper_slab");
	public static final TagKey<Item> COPPER_DOOR = create("copper_door");
	public static final TagKey<Item> COPPER_TRAPDOOR = create("copper_trapdoor");
	public static final TagKey<Item> COPPER_BULB = create("copper_bulb");

	public static final TagKey<Item> SHOP_BLACKLIST = create("shop_blacklist");

	public static final TagKey<Item> CORAL_BLOCKS = create("coral_block");
	public static final TagKey<Item> DEAD_CORAL_BLOCKS = create("dead_coral_block");
	public static final TagKey<Item> CORAL = create("coral");
	public static final TagKey<Item> DEAD_CORAL = create("dead_coral");
	public static final TagKey<Item> FROGLIGHT = create("froglight");
	public static final TagKey<Item> SHOWERING_ITEM = create("showering_item");
	public static final TagKey<Item> REDSTONE_RELATED = create("redstone_related");
	public static final TagKey<Item> ARCHERY_SEASON = create("archery_season");
	public static final TagKey<Item> VALUABLES = create("valuables");

	public CraftorioItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> future, CompletableFuture<TagLookup<Block>> provider, ExistingFileHelper helper) {
        super(output, future, provider, Craftorio.MODID, helper);

    }

	@SuppressWarnings("unchecked")
    @Override
	protected void addTags(HolderLookup.Provider provider) {

		tag(ARCHERY_SEASON).add(
				Items.BOW,
				Items.CROSSBOW,
				Items.ARROW,
				Items.SPECTRAL_ARROW,
				Items.TIPPED_ARROW
		);

		tag(VALUABLES).add(
				Items.DIAMOND,
				Items.DIAMOND_BLOCK,
				Items.NETHERITE_INGOT,
				Items.GOLD_INGOT,
				Items.GOLD_BLOCK,
				Items.ANCIENT_DEBRIS,
				Items.NETHER_STAR,
				Items.NETHERITE_BLOCK,
				Items.HEAVY_CORE,
				Items.MACE,
				Items.DRAGON_EGG,
				Items.ANCIENT_DEBRIS,
				Items.HEART_OF_THE_SEA
		);

		tag(SHOP_BLACKLIST).add(
				Items.BEDROCK,
				Items.BARRIER,
				Items.STRUCTURE_BLOCK,
				Items.STRUCTURE_VOID,
				Items.JIGSAW,
				Items.COMMAND_BLOCK,
				Items.CHAIN_COMMAND_BLOCK,
				Items.REPEATING_COMMAND_BLOCK,
				Items.COMMAND_BLOCK_MINECART,
				Items.REINFORCED_DEEPSLATE,
				Items.DEBUG_STICK,
				Items.LIGHT,
				Items.SPAWNER,
				Items.TRIAL_SPAWNER,
				Items.VAULT,
				Items.KNOWLEDGE_BOOK,
				Items.PETRIFIED_OAK_SLAB,
				Items.END_PORTAL_FRAME,
				Items.DRAGON_EGG,
				Items.POTION,
				Items.TIPPED_ARROW,
				Items.ENCHANTED_BOOK,
				CraftorioItems.EFFECT_RUNE.get(),
				CraftorioItems.MYSTERY_EFFECT_RUNE.get(),
				Items.WRITTEN_BOOK,
				CraftorioItems.SCANNER_STICK.get()
		);

		tag(FROGLIGHT).add(
				Blocks.OCHRE_FROGLIGHT.asItem(),
				Blocks.PEARLESCENT_FROGLIGHT.asItem(),
				Blocks.VERDANT_FROGLIGHT.asItem()

		);

		tag(SHOWERING_ITEM).add(
				Items.GOLD_INGOT
		);

		tag(REDSTONE_RELATED).add(
				Items.REDSTONE,
				Items.REDSTONE_BLOCK,
				Items.REDSTONE_TORCH,
				Items.REDSTONE_LAMP,
				Items.REDSTONE_ORE,
				Items.DEEPSLATE_REDSTONE_ORE,
				Items.REPEATER,
				Items.COMPARATOR,
				Items.PISTON,
				Items.STICKY_PISTON,
				Items.DISPENSER,
				Items.DROPPER,
				Items.HOPPER,
				Items.OBSERVER,
				Items.NOTE_BLOCK,
				Items.TARGET,
				Items.LEVER,
				Items.TRIPWIRE_HOOK,
				Items.DAYLIGHT_DETECTOR,
				Items.CRAFTER,
				Items.SCULK_SENSOR,
				Items.CALIBRATED_SCULK_SENSOR,
				Items.TNT,
				Items.TRAPPED_CHEST,
				Items.IRON_DOOR,
				Items.IRON_TRAPDOOR,
				Items.POWERED_RAIL,
				Items.DETECTOR_RAIL,
				Items.ACTIVATOR_RAIL,
				Items.OAK_BUTTON,
				Items.SPRUCE_BUTTON,
				Items.BIRCH_BUTTON,
				Items.JUNGLE_BUTTON,
				Items.ACACIA_BUTTON,
				Items.DARK_OAK_BUTTON,
				Items.MANGROVE_BUTTON,
				Items.CHERRY_BUTTON,
				Items.BAMBOO_BUTTON,
				Items.CRIMSON_BUTTON,
				Items.WARPED_BUTTON,
				Items.STONE_BUTTON,
				Items.POLISHED_BLACKSTONE_BUTTON,
				Items.OAK_PRESSURE_PLATE,
				Items.SPRUCE_PRESSURE_PLATE,
				Items.BIRCH_PRESSURE_PLATE,
				Items.JUNGLE_PRESSURE_PLATE,
				Items.ACACIA_PRESSURE_PLATE,
				Items.DARK_OAK_PRESSURE_PLATE,
				Items.MANGROVE_PRESSURE_PLATE,
				Items.CHERRY_PRESSURE_PLATE,
				Items.BAMBOO_PRESSURE_PLATE,
				Items.CRIMSON_PRESSURE_PLATE,
				Items.WARPED_PRESSURE_PLATE,
				Items.STONE_PRESSURE_PLATE,
				Items.POLISHED_BLACKSTONE_PRESSURE_PLATE,
				Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
				Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
				Blocks.COPPER_BULB.asItem(),
				Blocks.EXPOSED_COPPER_BULB.asItem(),
				Blocks.WEATHERED_COPPER_BULB.asItem(),
				Blocks.OXIDIZED_COPPER_BULB.asItem(),
				Blocks.WAXED_COPPER_BULB.asItem(),
				Blocks.WAXED_EXPOSED_COPPER_BULB.asItem(),
				Blocks.WAXED_WEATHERED_COPPER_BULB.asItem(),
				Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem()
		);

		tag(CORAL_BLOCKS).add(
				Blocks.TUBE_CORAL_BLOCK.asItem(),
				Blocks.BRAIN_CORAL_BLOCK.asItem(),
				Blocks.BUBBLE_CORAL_BLOCK.asItem(),
				Blocks.FIRE_CORAL_BLOCK.asItem(),
				Blocks.HORN_CORAL_BLOCK.asItem()
		);

		tag(DEAD_CORAL_BLOCKS).add(
				Blocks.DEAD_TUBE_CORAL_BLOCK.asItem(),
				Blocks.DEAD_BRAIN_CORAL_BLOCK.asItem(),
				Blocks.DEAD_BUBBLE_CORAL_BLOCK.asItem(),
				Blocks.DEAD_FIRE_CORAL_BLOCK.asItem(),
				Blocks.DEAD_HORN_CORAL_BLOCK.asItem()
		);

		tag(CORAL).add(
				Blocks.TUBE_CORAL.asItem(),
				Blocks.BRAIN_CORAL.asItem(),
				Blocks.BUBBLE_CORAL.asItem(),
				Blocks.FIRE_CORAL.asItem(),
				Blocks.HORN_CORAL.asItem(),
				Blocks.TUBE_CORAL_FAN.asItem(),
				Blocks.BRAIN_CORAL_FAN.asItem(),
				Blocks.BUBBLE_CORAL_FAN.asItem(),
				Blocks.FIRE_CORAL_FAN.asItem(),
				Blocks.HORN_CORAL_FAN.asItem()
		);

		tag(DEAD_CORAL).add(
				Blocks.DEAD_TUBE_CORAL.asItem(),
				Blocks.DEAD_BRAIN_CORAL.asItem(),
				Blocks.DEAD_BUBBLE_CORAL.asItem(),
				Blocks.DEAD_FIRE_CORAL.asItem(),
				Blocks.DEAD_HORN_CORAL.asItem(),
				Blocks.DEAD_TUBE_CORAL_FAN.asItem(),
				Blocks.DEAD_BRAIN_CORAL_FAN.asItem(),
				Blocks.DEAD_BUBBLE_CORAL_FAN.asItem(),
				Blocks.DEAD_FIRE_CORAL_FAN.asItem(),
				Blocks.DEAD_HORN_CORAL_FAN.asItem()
		);

		tag(COPPER).add(
				Blocks.COPPER_BLOCK.asItem(),
				Blocks.EXPOSED_COPPER.asItem(),
				Blocks.WEATHERED_COPPER.asItem(),
				Blocks.OXIDIZED_COPPER.asItem(),
				Blocks.WAXED_COPPER_BLOCK.asItem(),
				Blocks.WAXED_EXPOSED_COPPER.asItem(),
				Blocks.WAXED_WEATHERED_COPPER.asItem(),
				Blocks.WAXED_OXIDIZED_COPPER.asItem(),
				Items.COPPER_INGOT,
				Blocks.COPPER_ORE.asItem(),
				Blocks.DEEPSLATE_COPPER_ORE.asItem(),
				Items.RAW_COPPER,
				Blocks.RAW_COPPER_BLOCK.asItem()
		);


		tag(CHISELED_COPPER).add(
				Blocks.CHISELED_COPPER.asItem(),
				Blocks.EXPOSED_CHISELED_COPPER.asItem(),
				Blocks.WEATHERED_CHISELED_COPPER.asItem(),
				Blocks.OXIDIZED_CHISELED_COPPER.asItem(),
				Blocks.WAXED_CHISELED_COPPER.asItem(),
				Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem(),
				Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem(),
				Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem()
		);

		tag(COPPER_GRATE).add(
				Blocks.COPPER_GRATE.asItem(),
				Blocks.EXPOSED_COPPER_GRATE.asItem(),
				Blocks.WEATHERED_COPPER_GRATE.asItem(),
				Blocks.OXIDIZED_COPPER_GRATE.asItem(),
				Blocks.WAXED_COPPER_GRATE.asItem(),
				Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem(),
				Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem(),
				Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem()
		);

		tag(CUT_COPPER).add(
				Blocks.CUT_COPPER.asItem(),
				Blocks.EXPOSED_CUT_COPPER.asItem(),
				Blocks.WEATHERED_CUT_COPPER.asItem(),
				Blocks.OXIDIZED_CUT_COPPER.asItem(),
				Blocks.WAXED_CUT_COPPER.asItem(),
				Blocks.WAXED_EXPOSED_CUT_COPPER.asItem(),
				Blocks.WAXED_WEATHERED_CUT_COPPER.asItem(),
				Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem()
		);

		tag(CUT_COPPER_STAIRS).add(
				Blocks.CUT_COPPER_STAIRS.asItem(),
				Blocks.EXPOSED_CUT_COPPER_STAIRS.asItem(),
				Blocks.WEATHERED_CUT_COPPER_STAIRS.asItem(),
				Blocks.OXIDIZED_CUT_COPPER_STAIRS.asItem(),
				Blocks.WAXED_CUT_COPPER_STAIRS.asItem(),
				Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem(),
				Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem(),
				Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem()
		);

		tag(CUT_COPPER_SLAB).add(
				Blocks.CUT_COPPER_SLAB.asItem(),
				Blocks.EXPOSED_CUT_COPPER_SLAB.asItem(),
				Blocks.WEATHERED_CUT_COPPER_SLAB.asItem(),
				Blocks.OXIDIZED_CUT_COPPER_SLAB.asItem(),
				Blocks.WAXED_CUT_COPPER_SLAB.asItem(),
				Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem(),
				Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem(),
				Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem()
		);

		tag(COPPER_DOOR).add(
				Blocks.COPPER_DOOR.asItem(),
				Blocks.EXPOSED_COPPER_DOOR.asItem(),
				Blocks.WEATHERED_COPPER_DOOR.asItem(),
				Blocks.OXIDIZED_COPPER_DOOR.asItem(),
				Blocks.WAXED_COPPER_DOOR.asItem(),
				Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem(),
				Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem(),
				Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem()
		);

		tag(COPPER_TRAPDOOR).add(
				Blocks.COPPER_TRAPDOOR.asItem(),
				Blocks.EXPOSED_COPPER_TRAPDOOR.asItem(),
				Blocks.WEATHERED_COPPER_TRAPDOOR.asItem(),
				Blocks.OXIDIZED_COPPER_TRAPDOOR.asItem(),
				Blocks.WAXED_COPPER_TRAPDOOR.asItem(),
				Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem(),
				Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem(),
				Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem()
		);

		tag(COPPER_BULB).add(
				Blocks.COPPER_BULB.asItem(),
				Blocks.EXPOSED_COPPER_BULB.asItem(),
				Blocks.WEATHERED_COPPER_BULB.asItem(),
				Blocks.OXIDIZED_COPPER_BULB.asItem(),
				Blocks.WAXED_COPPER_BULB.asItem(),
				Blocks.WAXED_EXPOSED_COPPER_BULB.asItem(),
				Blocks.WAXED_WEATHERED_COPPER_BULB.asItem(),
				Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem()
		);
	}

	@Override
	protected void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
		super.copy(blockTag, itemTag);
	}

	@Override
	public String getName() {
		return "ACE Item Tags";
	}


	public static TagKey<Item> create(String tagName) {
		return ItemTags.create(Craftorio.prefix(tagName));
	}


}
