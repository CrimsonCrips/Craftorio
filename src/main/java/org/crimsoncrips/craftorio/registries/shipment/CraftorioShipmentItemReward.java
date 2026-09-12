package org.crimsoncrips.craftorio.registries.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.CraftorioDataComponents;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.util.ArrayList;
import java.util.List;

public class CraftorioShipmentItemReward {

    int amountGiving;
    ItemStack rewardingStack;
    int randomEffectCount;

    public static final Codec<CraftorioShipmentItemReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("amountGiving").forGetter(CraftorioShipmentItemReward::getAmountGiving),
                    ItemStack.CODEC.fieldOf("rewardingItem").forGetter(CraftorioShipmentItemReward::getRewardingStack),
                    Codec.INT.optionalFieldOf("randomEffectCount", 0).forGetter(CraftorioShipmentItemReward::getRandomEffectCount)
                    ).apply(instance, CraftorioShipmentItemReward::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftorioShipmentItemReward> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, CraftorioShipmentItemReward::getAmountGiving,
            ItemStack.STREAM_CODEC, CraftorioShipmentItemReward::getRewardingStack,
            ByteBufCodecs.INT, CraftorioShipmentItemReward::getRandomEffectCount,
            CraftorioShipmentItemReward::new
    );

    public CraftorioShipmentItemReward(int amountGiving, ItemStack rewardingStack, int randomEffectCount){
        this.amountGiving = amountGiving;
        this.rewardingStack = rewardingStack.copy();
        this.randomEffectCount = randomEffectCount;
    }

    public CraftorioShipmentItemReward(int amountGiving, ItemStack rewardingStack){
        this(amountGiving, rewardingStack, 0);
    }

    public CraftorioShipmentItemReward(int amountGiving, Item rewardingItem){
        this(amountGiving, new ItemStack(rewardingItem), 0);
    }

    public void giveItems(Player player){
        ItemStack stack = rewardingStack.copy();

        if (randomEffectCount > 0) {
            RandomSource random = player instanceof ServerPlayer serverPlayer ? serverPlayer.getRandom() : RandomSource.create();
            List<CraftorioEffects> rolled = new ArrayList<>();
            for (int i = 0; i < randomEffectCount; i++) {
                rolled.add(CraftorioMisc.getRandomEffect(player.registryAccess(), random));
            }
            stack.set(CraftorioDataComponents.EFFECTS_STORED.get(), rolled);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CraftorioMisc.giveItemsSplitByStack(serverPlayer, stack, amountGiving);
        } else {
            player.addItem(stack.copyWithCount(amountGiving));
        }
    }


    public CraftorioShipmentItemReward copy() {
        return new CraftorioShipmentItemReward(amountGiving, rewardingStack.copy(), randomEffectCount);
    }

    public int getAmountGiving() {
        return amountGiving;
    }

    public void setAmountGiving(int amountGiving) {
        this.amountGiving = amountGiving;
    }

    public ItemStack getRewardingStack() {
        return rewardingStack;
    }

    public void setRewardingStack(ItemStack rewardingStack) {
        this.rewardingStack = rewardingStack;
    }

    public int getRandomEffectCount() {
        return randomEffectCount;
    }

    public void setRandomEffectCount(int randomEffectCount) {
        this.randomEffectCount = randomEffectCount;
    }
}
