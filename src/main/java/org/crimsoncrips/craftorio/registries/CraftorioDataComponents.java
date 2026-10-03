package org.crimsoncrips.craftorio.registries;

import org.crimsoncrips.craftorio.registries.effect.StoredEffects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.item.structure.StructureWandSettings;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.math.BigInteger;

public class CraftorioDataComponents {
	public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Craftorio.MODID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredEffects>> EFFECTS_STORED = COMPONENTS.register("effects_stored", () -> DataComponentType.<StoredEffects>builder().persistent(StoredEffects.CODEC).networkSynchronized(StoredEffects.STREAM_CODEC).build());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BigInteger>> CONDENSED_VALUE =
			register("condensed_value", CraftorioMisc.BIGINT_CODEC(), ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SCAN_POS_1 =
			register("scan_pos_1", BlockPos.CODEC, ByteBufCodecs.fromCodec(BlockPos.CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SCAN_POS_2 =
			register("scan_pos_2", BlockPos.CODEC, ByteBufCodecs.fromCodec(BlockPos.CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SchematicData>> SCHEMATIC =
			register("schematic", SchematicData.CODEC, SchematicData.STREAM_CODEC);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<StructureWandSettings>> STRUCTURE_WAND =
			register("structure_wand", StructureWandSettings.CODEC, StructureWandSettings.STREAM_CODEC);



	private static @NotNull <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, final Codec<T> codec) {
		return register(name, codec, null);
	}

	private static @NotNull <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, final Codec<T> codec, @Nullable final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		if (streamCodec == null) {
			return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).build());
		} else {
			return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build());
		}
	}
}
