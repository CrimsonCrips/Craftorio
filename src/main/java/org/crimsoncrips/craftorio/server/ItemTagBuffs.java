package org.crimsoncrips.craftorio.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;

public record ItemTagBuffs(float multiplier, String name, String itemTag) {
	public static final Codec<ItemTagBuffs> DIRECT_CODEC = RecordCodecBuilder.create((recordCodecBuilder) -> recordCodecBuilder.group(
			ExtraCodecs.POSITIVE_FLOAT.fieldOf("multiplier").forGetter(ItemTagBuffs::multiplier),
			ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(ItemTagBuffs::name),
			ExtraCodecs.NON_EMPTY_STRING.fieldOf("itemTag").forGetter(ItemTagBuffs::itemTag)
	).apply(recordCodecBuilder, ItemTagBuffs::new));

	public static final Codec<Holder<ItemTagBuffs>> CODEC = RegistryFileCodec.create(CraftorioObjects.Keys.BLOCK_TAG_BUFFS, DIRECT_CODEC);


}
