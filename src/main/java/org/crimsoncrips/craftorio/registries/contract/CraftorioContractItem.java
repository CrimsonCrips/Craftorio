package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class CraftorioContractItem {

    int itemsGiven = 0;
    int amountRequired;
    Optional<Item> itemNeeded;
    Optional<TagKey<Item>> itemTag;
    Optional<ItemStack> stackNeeded;

    public static final Codec<CraftorioContractItem> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("amountRequired").forGetter(CraftorioContractItem::getAmountRequired),
                    BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("contract_item").forGetter(CraftorioContractItem::getItemNeeded),
                    TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("contract_tag").forGetter(CraftorioContractItem::getItemTag),
                    ItemStack.CODEC.optionalFieldOf("contract_item_stack").forGetter(CraftorioContractItem::getStackNeeded)
                    ).apply(instance, CraftorioContractItem::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftorioContractItem> CODEC_STREAM = StreamCodec.composite(
            ByteBufCodecs.INT, CraftorioContractItem::getAmountRequired,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(BuiltInRegistries.ITEM.byNameCodec())), CraftorioContractItem::getItemNeeded,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(TagKey.hashedCodec(Registries.ITEM))), CraftorioContractItem::getItemTag,
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC), CraftorioContractItem::getStackNeeded,
            CraftorioContractItem::new
    );

    public CraftorioContractItem(int amountRequired, Optional<Item> itemNeeded, Optional<TagKey<Item>> itemTag, Optional<ItemStack> stackNeeded){
        this.amountRequired = amountRequired;
        this.itemNeeded = itemNeeded;
        this.itemTag = itemTag;
        this.stackNeeded = stackNeeded;
    }

    public CraftorioContractItem(int amountRequired, Item itemNeeded){
        this(amountRequired, Optional.of(itemNeeded), Optional.empty(), Optional.empty());
    }

    public CraftorioContractItem(int amountRequired, TagKey<Item> itemTag){
        this(amountRequired, Optional.empty(), Optional.of(itemTag), Optional.empty());
    }

    public CraftorioContractItem(int amountRequired, ItemStack stackNeeded){
        this(amountRequired, Optional.empty(), Optional.empty(), Optional.of(stackNeeded.copy()));
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

    public Optional<ItemStack> getStackNeeded() {
        return stackNeeded;
    }

    public void setItemsGiven(int itemsGiven) {
        this.itemsGiven = itemsGiven;
    }

    public boolean isComplete(){
        return getItemsGiven() >= getAmountRequired();
    }

    public boolean matches(ItemStack stack) {
        if (stackNeeded.isPresent()) {
            return ItemStack.isSameItemSameComponents(stack, stackNeeded.get());
        }
        if (itemNeeded.isPresent()) {
            return itemNeeded.get().equals(stack.getItem());
        }
        return itemTag.isPresent() && stack.is(itemTag.get());
    }

    public ItemStack getIconStack() {
        if (stackNeeded.isPresent()) {
            return stackNeeded.get().copy();
        }
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
        if (stackNeeded.isPresent()) {
            return stackNeeded.get().getHoverName();
        }
        if (itemNeeded.isPresent()) {
            return itemNeeded.get().getDescription();
        }
        return itemTag.<Component>map(tag -> Component.literal("#" + tag.location()))
                .orElse(Component.literal("?"));
    }

    public CraftorioContractItem copy() {
        return new CraftorioContractItem(amountRequired, itemNeeded, itemTag, stackNeeded.map(ItemStack::copy));
    }
}
