package org.crimsoncrips.craftorio.registries.contracts.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;


import java.util.ArrayList;
import java.util.List;

public class CraftorioShipmentContract {

    private String name;
    private int time;

    public static final ResourceKey<Registry<CraftorioShipmentContract>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "shipment_contract"));

    List<CraftorioShipmentItem> itemBounty;

    public static final Codec<CraftorioShipmentContract> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CraftorioShipmentItem.CODEC).fieldOf("itemBounty").forGetter(CraftorioShipmentContract::getItemBounty),
                    Codec.STRING.fieldOf("name").forGetter(CraftorioShipmentContract::getName),
                    Codec.INT.fieldOf("time").forGetter(CraftorioShipmentContract::getTime)
            ).apply(instance, CraftorioShipmentContract::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentContract> CODEC_STREAM = StreamCodec.composite(
            CraftorioShipmentItem.CODEC_STREAM.apply(ByteBufCodecs.list()), CraftorioShipmentContract::getItemBounty,
            ByteBufCodecs.STRING_UTF8, CraftorioShipmentContract::getName,
            ByteBufCodecs.INT, CraftorioShipmentContract::getTime,
            CraftorioShipmentContract::new
    );

    public CraftorioShipmentContract(List<CraftorioShipmentItem> itemBounty,String name, int time){
        this.itemBounty = itemBounty;
        this.name = name;
        this.time = time;
    }

    public boolean isComplete(Player player){
        boolean finished = true;
        for (CraftorioShipmentItem contractItem : itemBounty){
            if (!contractItem.isComplete()){
                finished = false;
            }
        }
        if (finished){
            List<CraftorioShipmentContract> newContract = CraftorioMisc.getCraftorioContracts(player);
            newContract.remove(this);
            CraftorioMisc.setCraftorioContracts(player,newContract);
        }
        return finished;
    }

    public CraftorioShipmentContract copy() {
        List<CraftorioShipmentItem> copiedItems = new ArrayList<>(itemBounty);
        return new CraftorioShipmentContract(copiedItems,getName(),getTime());
    }

    public void addSinkedListValue(List<ItemStack> itemsSinked, Player player){
        for (ItemStack itemStack : itemsSinked){
            for (CraftorioShipmentItem contractItem : getItemBounty()){
                if (contractItem.getItemNeeded().equals(itemStack.getItem()) && !contractItem.isComplete()){
                    contractItem.setItemsGiven(contractItem.getItemsGiven() + itemStack.getCount());
                }
            }
        }
        isComplete(player);
    }

    public void setItemBounty(List<CraftorioShipmentItem> itemBounty) {
        this.itemBounty = itemBounty;
    }

    public List<CraftorioShipmentItem> getItemBounty() {
        return itemBounty;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void tick(){
        setTime(time - 1);
    }
}