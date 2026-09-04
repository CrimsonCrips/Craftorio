package org.crimsoncrips.craftorio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.List;

public record PollMetaData(String name, int choiceLimit, List<Component> choices) {

	public static final Codec<PollMetaData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(PollMetaData::name),
			Codec.INT.fieldOf("choice_limit").forGetter(PollMetaData::choiceLimit),
			ComponentSerialization.CODEC.listOf().fieldOf("choices").forGetter(PollMetaData::choices)
	).apply(inst, PollMetaData::new));

	public List<String> stringChoices() {
		return this.choices.stream().map(Component::getString).toList();
	}

}