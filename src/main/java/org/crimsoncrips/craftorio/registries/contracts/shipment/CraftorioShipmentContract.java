package org.crimsoncrips.craftorio.registries.contracts.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CraftorioShipmentContract {


    List<CraftorioContractItem> itemBounty;

    public static final Codec<CraftorioShipmentContract> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CraftorioContractItem.CODEC).fieldOf("itemBounty").forGetter(CraftorioShipmentContract::getItemBounty)
            ).apply(instance, CraftorioShipmentContract::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentContract> CODEC_STREAM = StreamCodec.composite(
            CraftorioContractItem.CODEC_STREAM.apply(ByteBufCodecs.list()), CraftorioShipmentContract::getItemBounty,
            CraftorioShipmentContract::new
    );

    public CraftorioShipmentContract(List<CraftorioContractItem> itemBounty){
        this.itemBounty = itemBounty;
    }

    public boolean isComplete(Player player){
        boolean finished = true;
        for (CraftorioContractItem contractItem : itemBounty){
            if (!contractItem.isComplete()){
                finished = false;
            }
        }
        if (finished){
            System.out.println("COMPLETE");
        }
        return finished;
    }

    public void addSinkedListValue(List<ItemStack> itemsSinked, Player player){
        for (ItemStack itemStack : itemsSinked){
            for (CraftorioContractItem contractItem : getItemBounty()){
                if (contractItem.getItemNeeded().equals(itemStack.getItem()) && !contractItem.isComplete()){
                    contractItem.setItemsGiven(contractItem.getItemsGiven() + itemStack.getCount());
                }
            }
        }
        isComplete(player);
    }

    public void setItemBounty(List<CraftorioContractItem> itemBounty) {
        this.itemBounty = itemBounty;
    }

    public List<CraftorioContractItem> getItemBounty() {
        return itemBounty;
    }
}
