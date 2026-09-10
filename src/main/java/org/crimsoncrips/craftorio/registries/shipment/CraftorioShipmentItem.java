package org.crimsoncrips.craftorio.registries.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class CraftorioShipmentItem {

    int itemsGiven = 0;
    int amountRequired;
    Optional<Item> itemNeeded;
    Optional<TagKey<Item>> itemTag;

    public static final Codec<CraftorioShipmentItem> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("amountRequired").forGetter(CraftorioShipmentItem::getAmountRequired),
                    BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("contract_item").forGetter(CraftorioShipmentItem::getItemNeeded),
                    TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("contract_tag").forGetter(CraftorioShipmentItem::getItemTag)
                    ).apply(instance, CraftorioShipmentItem::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentItem> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, CraftorioShipmentItem::getAmountRequired,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(BuiltInRegistries.ITEM.byNameCodec())), CraftorioShipmentItem::getItemNeeded,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(TagKey.hashedCodec(Registries.ITEM))), CraftorioShipmentItem::getItemTag,
            CraftorioShipmentItem::new
    );

    public CraftorioShipmentItem(int amountRequired, Optional<Item> itemNeeded, Optional<TagKey<Item>> itemTag){
        this.amountRequired = amountRequired;
        this.itemNeeded = itemNeeded;
        this.itemTag = itemTag;
    }

    public CraftorioShipmentItem(int amountRequired, Item itemNeeded){
        this(amountRequired, Optional.of(itemNeeded), Optional.empty());
    }

    public CraftorioShipmentItem(int amountRequired, TagKey<Item> itemTag){
        this(amountRequired, Optional.empty(), Optional.of(itemTag));
    }

    public int getItemsGiven() {
        return itemsGiven;
    }

    public int getAmountRequired() {
        return amountRequired;
    }

    public Optional<Item> getItemNeeded() {
        return itemNeeded;
    }

    public Optional<TagKey<Item>> getItemTag() {
        return itemTag;
    }

    public void setItemsGiven(int itemsGiven) {
        this.itemsGiven = itemsGiven;
    }

    public boolean isComplete(){
        return getItemsGiven() >= getAmountRequired();
    }

    public boolean matches(ItemStack stack) {
        if (itemNeeded.isPresent()) {
            return itemNeeded.get().equals(stack.getItem());
        }
        return itemTag.isPresent() && stack.is(itemTag.get());
    }

    public ItemStack getIconStack() {
        if (itemNeeded.isPresent()) {
            return new ItemStack(itemNeeded.get());
        }
        return itemTag.flatMap(tag -> BuiltInRegistries.ITEM.getTag(tag))
                .flatMap(holders -> holders.stream().findFirst())
                .map(Holder::value)
                .map(ItemStack::new)
                .orElse(new ItemStack(Items.BARRIER));
    }

    public Component getDisplayName() {
        if (itemNeeded.isPresent()) {
            return itemNeeded.get().getDescription();
        }
        return itemTag.<Component>map(tag -> Component.literal("#" + tag.location()))
                .orElse(Component.literal("?"));
    }

    public CraftorioShipmentItem copy() {
        return new CraftorioShipmentItem(amountRequired, itemNeeded, itemTag);
    }
}
