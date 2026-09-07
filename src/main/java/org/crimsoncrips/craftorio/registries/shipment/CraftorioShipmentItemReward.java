package org.crimsoncrips.craftorio.registries.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftorioShipmentItemReward {


    int amountGiving;
    Item rewardingItem;

    public static final Codec<CraftorioShipmentItemReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("amountGiving").forGetter(CraftorioShipmentItemReward::getAmountGiving),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("rewardingItem").forGetter(CraftorioShipmentItemReward::getRewardingItem)
                    ).apply(instance, CraftorioShipmentItemReward::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentItemReward> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, CraftorioShipmentItemReward::getAmountGiving,
            ByteBufCodecs.fromCodec(BuiltInRegistries.ITEM.byNameCodec()), CraftorioShipmentItemReward::getRewardingItem,
            CraftorioShipmentItemReward::new
    );

    public CraftorioShipmentItemReward(int amountGiving, Item rewardingItem){
        this.amountGiving = amountGiving;
        this.rewardingItem = rewardingItem;
    }

    public void giveItems(Player player){
        player.addItem(new ItemStack(rewardingItem,amountGiving));
    }


    public CraftorioShipmentItemReward copy() {
        return new CraftorioShipmentItemReward(amountGiving, rewardingItem);
    }

    public int getAmountGiving() {
        return amountGiving;
    }

    public void setAmountGiving(int amountGiving) {
        this.amountGiving = amountGiving;
    }

    public Item getRewardingItem() {
        return rewardingItem;
    }

    public void setRewardingItem(Item rewardingItem) {
        this.rewardingItem = rewardingItem;
    }
}
