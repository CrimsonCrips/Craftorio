package org.crimsoncrips.craftorio.skill_tree.upgrade_types.manual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;
import java.util.Optional;

public abstract class ActionUpgrade extends CraftorioUpgrade {

    protected ActionUpgrade(String name, ResourceLocation icon, ResourceLocation parent, String description, BigInteger cost) {
        super(name, icon, parent, description, cost, 1);
    }

    public void activateFunction(){

    };

    @Override
    public void onUnlock(ServerPlayer player, ResourceLocation id, int purchaseCount) {
        if (purchaseCount != 1) return;
        activateFunction();
    }

    public record Common(String name, ResourceLocation icon, Optional<ResourceLocation> parent, String description, BigInteger cost, double x, double y) {

        public static final MapCodec<Common> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.fieldOf("name").forGetter(Common::name),
                        ResourceLocation.CODEC.fieldOf("icon").forGetter(Common::icon),
                        ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Common::parent),
                        Codec.STRING.fieldOf("description").forGetter(Common::description),
                        CraftorioMisc.SCIENTIFIC_BIGINT_CODEC().fieldOf("cost").forGetter(Common::cost),
                        Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(Common::x),
                        Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(Common::y)
                ).apply(instance, Common::new)
        );
    }
}
