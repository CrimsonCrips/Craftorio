package org.crimsoncrips.craftorio.registries.contracts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CraftorioContract {


    List<CraftorioContractItem> itemBounty;

    public static final Codec<CraftorioContract> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CraftorioContractItem.CODEC).fieldOf("itemBounty").forGetter(CraftorioContract::getItemBounty)
            ).apply(instance, CraftorioContract::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioContract> CODEC_STREAM = StreamCodec.composite(
            CraftorioContractItem.CODEC_STREAM.apply(ByteBufCodecs.list()), CraftorioContract::getItemBounty,
            CraftorioContract::new
    );

    public CraftorioContract(List<CraftorioContractItem> itemBounty){
        this.itemBounty = itemBounty;
    }

    public boolean isComplete(){
        boolean finished = true;
        for (CraftorioContractItem contractItem : itemBounty){
            if (!contractItem.isComplete()){
                finished = false;
            }
        }
        return finished;
    }

    public void addSinkedListValue(List<ItemStack> itemsSinked){
        for (ItemStack itemStack : itemsSinked){
            for (CraftorioContractItem contractItem : getItemBounty()){
                if (contractItem.getItemNeeded().equals(itemStack.getItem()) && !contractItem.isComplete()){
                    contractItem.setItemsGiven(contractItem.getItemsGiven() + itemStack.getCount());
                }
            }
        }
    }

    public void setItemBounty(List<CraftorioContractItem> itemBounty) {
        this.itemBounty = itemBounty;
    }

    public List<CraftorioContractItem> getItemBounty() {
        return itemBounty;
    }
}
