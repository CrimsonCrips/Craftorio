package org.crimsoncrips.craftorio;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;

public class CraftorioDataComponents {
	public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Craftorio.MODID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<CraftorioEffects>>> EFFECTS_STORED = COMPONENTS.register("effects_stored", () -> DataComponentType.<List<CraftorioEffects>>builder().persistent(Codec.list(CraftorioEffects.dispatchCodec())).networkSynchronized(ByteBufCodecs.fromCodec(Codec.list(CraftorioEffects.dispatchCodec()))).build());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BigInteger>> CONDENSED_VALUE =
			register("condensed_value", CraftorioMisc.BIGINT_CODEC(), ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()));



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
