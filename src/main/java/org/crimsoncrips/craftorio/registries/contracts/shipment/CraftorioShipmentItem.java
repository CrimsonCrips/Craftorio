package org.crimsoncrips.craftorio.registries.contracts.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public class CraftorioShipmentItem {

    int itemsGiven = 0;
    int amountRequired;
    Item itemNeeded;

    public static final Codec<CraftorioShipmentItem> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("amountRequired").forGetter(CraftorioShipmentItem::getAmountRequired),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("contract_item").forGetter(CraftorioShipmentItem::getItemNeeded)
                    ).apply(instance, CraftorioShipmentItem::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentItem> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, CraftorioShipmentItem::getAmountRequired,
            ByteBufCodecs.fromCodec(BuiltInRegistries.ITEM.byNameCodec()), CraftorioShipmentItem::getItemNeeded,
            CraftorioShipmentItem::new
    );

    public CraftorioShipmentItem(int amountRequired, Item itemNeeded){
        this.amountRequired = amountRequired;
        this.itemNeeded = itemNeeded;
    }

    public int getItemsGiven() {
        return itemsGiven;
    }

    public int getAmountRequired() {
        return amountRequired;
    }

    public Item getItemNeeded() {
        return itemNeeded;
    }

    public void setAmountRequired(Item itemNeeded) {
        this.itemNeeded = itemNeeded;
    }

    public void setItemsGiven(int itemsGiven) {
        this.itemsGiven = itemsGiven;
    }


    public boolean isComplete(){
        return getItemsGiven() >= getAmountRequired();
    }

    public CraftorioShipmentItem copy() {
        return new CraftorioShipmentItem(amountRequired, itemNeeded);
    }
}
