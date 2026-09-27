package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ContractType implements StringRepresentable {
    SINK,
    BUILDING;

    public static final Codec<ContractType> CODEC = StringRepresentable.fromEnum(ContractType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
