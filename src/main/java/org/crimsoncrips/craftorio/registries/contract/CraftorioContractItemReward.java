package org.crimsoncrips.craftorio.registries.contract;

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
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.rune.EffectRune;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.StoredEffects;

import java.util.ArrayList;
import java.util.List;

public class CraftorioContractItemReward {

    int amountGiving;
    ItemStack rewardingStack;
    int randomEffectCount;

    public static final Codec<CraftorioContractItemReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("amountGiving").forGetter(CraftorioContractItemReward::getAmountGiving),
                    ItemStack.CODEC.fieldOf("rewardingItem").forGetter(CraftorioContractItemReward::getRewardingStack),
                    Codec.INT.optionalFieldOf("randomEffectCount", 0).forGetter(CraftorioContractItemReward::getRandomEffectCount)
                    ).apply(instance, CraftorioContractItemReward::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftorioContractItemReward> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, CraftorioContractItemReward::getAmountGiving,
            ItemStack.STREAM_CODEC, CraftorioContractItemReward::getRewardingStack,
            ByteBufCodecs.INT, CraftorioContractItemReward::getRandomEffectCount,
            CraftorioContractItemReward::new
    );

    public CraftorioContractItemReward(int amountGiving, ItemStack rewardingStack, int randomEffectCount){
        this.amountGiving = amountGiving;
        this.rewardingStack = rewardingStack.copy();
        this.randomEffectCount = randomEffectCount;
    }

    public CraftorioContractItemReward(int amountGiving, ItemStack rewardingStack){
        this(amountGiving, rewardingStack, 0);
    }

    public CraftorioContractItemReward(int amountGiving, Item rewardingItem){
        this(amountGiving, new ItemStack(rewardingItem), 0);
    }

    public CraftorioContractItemReward(int amountGiving, Item rewardingItem, int randomEffectCount){
        this(amountGiving, new ItemStack(rewardingItem), randomEffectCount);
    }

    public boolean rollsEffects() {
        return randomEffectCount > 0 && rewardingStack.getItem() instanceof EffectRune;
    }

    public void giveItems(Player player){
        if (rollsEffects()) {
            RandomSource random = player instanceof ServerPlayer serverPlayer ? serverPlayer.getRandom() : RandomSource.create();

            int remaining = amountGiving;
            while (remaining > 0) {
                ItemStack stack = rewardingStack.copy();
                int amount = Math.min(remaining, stack.getMaxStackSize());

                List<CraftorioEffects> rolled = new ArrayList<>();
                for (int j = 0; j < randomEffectCount; j++) {
                    rolled.add(CraftorioMisc.getRandomEffect(player.registryAccess(), random));
                }
                stack.set(CraftorioDataComponents.EFFECTS_STORED.get(), new StoredEffects(rolled));

                if (player instanceof ServerPlayer serverPlayer) {
                    CraftorioMisc.giveItemsSplitByStack(serverPlayer, stack, amount);
                } else {
                    player.addItem(stack.copyWithCount(amount));
                }
                remaining -= amount;
            }
        } else {
            ItemStack stack = rewardingStack.copy();

            if (player instanceof ServerPlayer serverPlayer) {
                CraftorioMisc.giveItemsSplitByStack(serverPlayer, stack, amountGiving);
            } else {
                player.addItem(stack.copyWithCount(amountGiving));
            }
        }
    }


    public CraftorioContractItemReward copy() {
        return new CraftorioContractItemReward(amountGiving, rewardingStack.copy(), randomEffectCount);
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
