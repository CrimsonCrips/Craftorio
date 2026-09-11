package org.crimsoncrips.craftorio.registries.shipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.crimsoncrips.craftorio.CraftorioMisc.BIGINT_CODEC;
import static org.crimsoncrips.craftorio.CraftorioMisc.SCIENTIFIC_BIGINT_CODEC;
import static org.crimsoncrips.craftorio.CraftorioMisc.SECONDS_TO_TICKS;

public class CraftorioShipmentContract {

    private String name;
    private String description;
    private int time;
    private BigInteger pointRewards;
    private List<CraftorioShipmentItemReward> rewards;
    private ResourceLocation icon;
    private ResourceLocation punishment;
    private boolean abandoned;
    private int weight;
    private BigInteger pointThreshold;
    private BigInteger minPointThreshold;
    private BigInteger maxPointThreshold;

    public static final ResourceKey<Registry<CraftorioShipmentContract>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "shipment_contract"));

    List<CraftorioShipmentItem> itemBounty;

    public static final Codec<CraftorioShipmentContract> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CraftorioShipmentItem.CODEC).fieldOf("itemBounty").forGetter(CraftorioShipmentContract::getItemBounty),
                    Codec.STRING.fieldOf("name").forGetter(CraftorioShipmentContract::getActualName),
                    Codec.STRING.fieldOf("description").forGetter(CraftorioShipmentContract::getDescription),
                    Codec.INT.fieldOf("seconds").forGetter(contract -> contract.getTime() / SECONDS_TO_TICKS),
                    BIGINT_CODEC().optionalFieldOf("pointRewards", BigInteger.ZERO).forGetter(CraftorioShipmentContract::getPointRewards),
                    Codec.list(CraftorioShipmentItemReward.CODEC).optionalFieldOf("itemRewards", List.of()).forGetter(CraftorioShipmentContract::getRewards),
                    ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(contract -> Optional.ofNullable(contract.getIcon())),
                    ResourceLocation.CODEC.optionalFieldOf("punishment").forGetter(contract -> Optional.ofNullable(contract.getPunishment())),
                    Codec.INT.fieldOf("weight").forGetter(CraftorioShipmentContract::getWeight),
                    SCIENTIFIC_BIGINT_CODEC().optionalFieldOf("point_threshold", BigInteger.ZERO).forGetter(CraftorioShipmentContract::getPointThreshold),
                    SCIENTIFIC_BIGINT_CODEC().optionalFieldOf("min_point_threshold", BigInteger.ZERO).forGetter(CraftorioShipmentContract::getMinPointThreshold),
                    SCIENTIFIC_BIGINT_CODEC().optionalFieldOf("max_point_threshold", CraftorioMisc.pointThreshold()).forGetter(CraftorioShipmentContract::getMaxPointThreshold)
            ).apply(instance, (itemBounty, name, description, seconds, pointRewards, rewards, icon, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold) ->
                    new CraftorioShipmentContract(itemBounty, name, description, seconds, pointRewards, rewards, icon.orElse(null), punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold))
    );

    public static final StreamCodec<ByteBuf, CraftorioShipmentContract> CODEC_STREAM = StreamCodec.of(
            (buffer, contract) -> {
                CraftorioShipmentItem.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, contract.getItemBounty());
                ByteBufCodecs.STRING_UTF8.encode(buffer, contract.getActualName());
                ByteBufCodecs.STRING_UTF8.encode(buffer, contract.getDescription());
                ByteBufCodecs.INT.encode(buffer, contract.getTime());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getPointRewards());
                CraftorioShipmentItemReward.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, contract.getRewards());
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, Optional.ofNullable(contract.getIcon()));
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, Optional.ofNullable(contract.getPunishment()));
                ByteBufCodecs.BOOL.encode(buffer, contract.isAbandoned());
                ByteBufCodecs.INT.encode(buffer, contract.getWeight());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getPointThreshold());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getMinPointThreshold());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getMaxPointThreshold());
            },
            buffer -> {
                List<CraftorioShipmentItem> itemBounty = CraftorioShipmentItem.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                String name = ByteBufCodecs.STRING_UTF8.decode(buffer);
                String description = ByteBufCodecs.STRING_UTF8.decode(buffer);
                int time = ByteBufCodecs.INT.decode(buffer);
                BigInteger pointRewards = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                List<CraftorioShipmentItemReward> rewards = CraftorioShipmentItemReward.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                ResourceLocation icon = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer).orElse(null);
                Optional<ResourceLocation> punishment = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer);
                boolean abandoned = ByteBufCodecs.BOOL.decode(buffer);
                int weight = ByteBufCodecs.INT.decode(buffer);
                BigInteger pointThreshold = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger minPointThreshold = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger maxPointThreshold = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                CraftorioShipmentContract contract = new CraftorioShipmentContract(itemBounty, name, description, time / SECONDS_TO_TICKS, pointRewards, rewards, icon, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold);
                contract.setTime(time);
                return contract;
            }
    );

    public CraftorioShipmentContract(List<CraftorioShipmentItem> itemBounty, String name, String description, int seconds, BigInteger pointRewards, List<CraftorioShipmentItemReward> rewards, ResourceLocation icon, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold){
        this.itemBounty = itemBounty;
        this.name = name;
        this.description = description;
        this.time = seconds * SECONDS_TO_TICKS;
        this.pointRewards = pointRewards != null ? pointRewards : BigInteger.ZERO;
        this.rewards = rewards != null ? rewards : List.of();
        this.icon = icon;
        this.punishment = punishment.orElse(null);
        this.abandoned = false;
        this.weight = weight;
        this.pointThreshold = pointThreshold;
        this.minPointThreshold = minPointThreshold != null ? minPointThreshold : BigInteger.ZERO;
        this.maxPointThreshold = maxPointThreshold != null ? maxPointThreshold : CraftorioMisc.pointThreshold();
    }

    public CraftorioShipmentContract(List<CraftorioShipmentItem> itemBounty, String langKey, int seconds, BigInteger pointRewards, List<CraftorioShipmentItemReward> rewards, ResourceLocation icon, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold){
        this(itemBounty, "registry." + langKey + ".title", "registry." + langKey + ".description", seconds, pointRewards, rewards, icon, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold);
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
        CraftorioShipmentContract copy = new CraftorioShipmentContract(copiedItems, getActualName(), getDescription(), getTime() / SECONDS_TO_TICKS, getPointRewards(), getRewards(), getIcon(), Optional.ofNullable(getPunishment()), getWeight(), getPointThreshold(), getMinPointThreshold(), getMaxPointThreshold());
        copy.setTime(getTime());
        return copy;
    }

    public boolean shouldEnd(){
        return isComplete() || time <= 0;
    }

    public void addSinkedListValue(List<ItemStack> itemsSinked, Player player){
        for (ItemStack itemStack : itemsSinked){
            for (CraftorioShipmentItem contractItem : getItemBounty()){
                if (!contractItem.isComplete() && contractItem.matches(itemStack)){
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

    public String getActualName(){
        return Component.translatable(name).getString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActualDescription() {
        return Component.translatable(description).getString();
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

    public ResourceLocation getIcon() {
        return icon;
    }

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
    }

    public ResourceLocation getPunishment() {
        return punishment;
    }

    public void setPunishment(ResourceLocation punishment) {
        this.punishment = punishment;
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public void setAbandoned(boolean abandoned) {
        this.abandoned = abandoned;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public BigInteger getPointThreshold() {
        return pointThreshold;
    }

    public void setPointThreshold(BigInteger pointThreshold) {
        this.pointThreshold = pointThreshold;
    }

    public BigInteger getMinPointThreshold() {
        return minPointThreshold;
    }

    public void setMinPointThreshold(BigInteger minPointThreshold) {
        this.minPointThreshold = minPointThreshold;
    }

    public BigInteger getMaxPointThreshold() {
        return maxPointThreshold;
    }

    public void setMaxPointThreshold(BigInteger maxPointThreshold) {
        this.maxPointThreshold = maxPointThreshold;
    }

    public void abandon() {
        this.abandoned = true;
        setTime(0);
    }

    public void tick(Player player){
        setTime(time - 1);
        if (shouldEnd()){
            if (isComplete()){
                CraftorioMisc.setPoints(getPointRewards().add(CraftorioMisc.getPoints(player)),player);
                for (CraftorioShipmentItemReward itemReward : getRewards()){
                    itemReward.giveItems(player);
                }
            } else {
                setTime(0);
                if (getPunishment() != null) {
                    CraftorioMisc.punishContractFailure(player, this, getPunishment());
                }
            }

            List<CraftorioShipmentContract> newContract = new ArrayList<>(CraftorioMisc.getCraftorioContracts(player));
            newContract.remove(this);
            CraftorioMisc.setCraftorioContracts(player,newContract);
        }
    }

}
