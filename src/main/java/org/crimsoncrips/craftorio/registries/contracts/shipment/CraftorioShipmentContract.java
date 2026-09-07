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


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.crimsoncrips.craftorio.CraftorioMisc.BIGINT_CODEC;

public class CraftorioShipmentContract {

    private String name;
    private int time;
    private BigInteger pointRewards;
    private List<CraftorioShipmentItemReward> rewards;

    public static final ResourceKey<Registry<CraftorioShipmentContract>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "shipment_contract"));

    List<CraftorioShipmentItem> itemBounty;

    public static final Codec<CraftorioShipmentContract> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CraftorioShipmentItem.CODEC).fieldOf("itemBounty").forGetter(CraftorioShipmentContract::getItemBounty),
                    Codec.STRING.fieldOf("name").forGetter(CraftorioShipmentContract::getName),
                    Codec.INT.fieldOf("time").forGetter(CraftorioShipmentContract::getTime),
                    BIGINT_CODEC().fieldOf("pointRewards").forGetter(CraftorioShipmentContract::getPointRewards),
                    Codec.list(CraftorioShipmentItemReward.CODEC).fieldOf("itemRewards").forGetter(CraftorioShipmentContract::getRewards)
            ).apply(instance, CraftorioShipmentContract::new)
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentContract> CODEC_STREAM = StreamCodec.composite(
            CraftorioShipmentItem.CODEC_STREAM.apply(ByteBufCodecs.list()), CraftorioShipmentContract::getItemBounty,
            ByteBufCodecs.STRING_UTF8, CraftorioShipmentContract::getName,
            ByteBufCodecs.INT, CraftorioShipmentContract::getTime,
            ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()),CraftorioShipmentContract::getPointRewards,
            CraftorioShipmentItemReward.CODEC_STREAM.apply(ByteBufCodecs.list()), CraftorioShipmentContract::getRewards,
            CraftorioShipmentContract::new
    );

    public CraftorioShipmentContract(List<CraftorioShipmentItem> itemBounty,String name, int time,BigInteger pointRewards,List<CraftorioShipmentItemReward> rewards){
        this.itemBounty = itemBounty;
        this.name = name;
        this.time = time;
        this.pointRewards = pointRewards;
        this.rewards = rewards;
    }

    public boolean isComplete(){
        boolean finished = true;
        for (CraftorioShipmentItem contractItem : itemBounty){
            if (!contractItem.isComplete()){
                finished = false;
            }
        }
        return finished;
    }

    public CraftorioShipmentContract copy() {
        List<CraftorioShipmentItem> copiedItems = new ArrayList<>();
        for (CraftorioShipmentItem item : itemBounty) {
            copiedItems.add(item.copy());
        }
        return new CraftorioShipmentContract(copiedItems, getName(), getTime(),getPointRewards(),getRewards());
    }

    public boolean shouldEnd(){
        return isComplete() || time <= 0;
    }

    public void addSinkedListValue(List<ItemStack> itemsSinked, Player player){
        for (ItemStack itemStack : itemsSinked){
            for (CraftorioShipmentItem contractItem : getItemBounty()){
                if (contractItem.getItemNeeded().equals(itemStack.getItem()) && !contractItem.isComplete()){
                    contractItem.setItemsGiven(contractItem.getItemsGiven() + itemStack.getCount());
                }
            }
        }
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

    public BigInteger getPointRewards() {
        return pointRewards;
    }

    public void setPointRewards(BigInteger pointRewards) {
        this.pointRewards = pointRewards;
    }

    public List<CraftorioShipmentItemReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<CraftorioShipmentItemReward> rewards) {
        this.rewards = rewards;
    }

    public void tick(Player player){
        if (shouldEnd()){
            if (isComplete()){
                CraftorioMisc.setPoints(getPointRewards().add(CraftorioMisc.getPoints(player)),player);
                for (CraftorioShipmentItemReward itemReward : getRewards()){
                    itemReward.giveItems(player);
                }
            }

            List<CraftorioShipmentContract> newContract = CraftorioMisc.getCraftorioContracts(player);
            newContract.remove(this);
            CraftorioMisc.setCraftorioContracts(player,newContract);
        }
        setTime(time - 1);
    }

}