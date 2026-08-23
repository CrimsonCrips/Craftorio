package org.crimsoncrips.craftorio.datagen.tags;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.crimsoncrips.craftorio.Craftorio;


import java.util.concurrent.CompletableFuture;

public class CraftItemTagGen extends ItemTagsProvider {
	public static final TagKey<Item> COPPER  = create("copper");
	public static final TagKey<Item> CHISELED_COPPER = create("chiseled_copper");
	public static final TagKey<Item> COPPER_GRATE = create("copper_grate");
	public static final TagKey<Item> CUT_COPPER = create("cut_copper");
	public static final TagKey<Item> CUT_COPPER_STAIRS = create("cut_copper_stairs");
	public static final TagKey<Item> CUT_COPPER_SLAB = create("cut_copper_slab");
	public static final TagKey<Item> COPPER_DOOR = create("copper_door");
	public static final TagKey<Item> COPPER_TRAPDOOR = create("copper_trapdoor");
	public static final TagKey<Item> COPPER_BULB = create("copper_bulb");

	public static final TagKey<Item> CORAL_BLOCKS = create("coral_block");
	public static final TagKey<Item> DEAD_CORAL_BLOCKS = create("dead_coral_block");
	public static final TagKey<Item> CORAL = create("coral");
	public static final TagKey<Item> DEAD_CORAL = create("dead_coral");
	public static final TagKey<Item> FROGLIGHT = create("froglight");


	public CraftItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> future, CompletableFuture<TagLookup<Block>> provider, ExistingFileHelper helper) {
        super(output, future, provider, Craftorio.MODID, helper);
    }

	@SuppressWarnings("unchecked")
    @Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(FROGLIGHT).add(
				Blocks.OCHRE_FROGLIGHT.asItem(),
				Blocks.PEARLESCENT_FROGLIGHT.asItem(),
				Blocks.VERDANT_FROGLIGHT.asItem()

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
				Blocks.WAXED_OXIDIZED_COPPER.asItem()
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
