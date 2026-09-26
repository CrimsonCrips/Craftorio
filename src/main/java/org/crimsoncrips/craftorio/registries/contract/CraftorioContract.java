package org.crimsoncrips.craftorio.registries.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.crimsoncrips.craftorio.CraftorioMisc.BIGINT_CODEC;
import static org.crimsoncrips.craftorio.CraftorioMisc.SCIENTIFIC_BIGINT_CODEC;
import static org.crimsoncrips.craftorio.CraftorioMisc.SECONDS_TO_TICKS;

public class CraftorioContract {

    private String name;
    private String description;
    private int time;
    private BigInteger basePointValue;
    private List<CraftorioContractItemReward> rewards;
    private ResourceLocation punishment;
    private boolean abandoned;
    private int weight;
    private BigInteger pointThreshold;
    private BigInteger minPointThreshold;
    private BigInteger maxPointThreshold;
    private String requiredModId;
    private ResourceKey<CraftorioContractTexture> cardTexture;
    private ContractTextColors textColors = ContractTextColors.EMPTY;

    public static final ResourceKey<Registry<CraftorioContract>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Craftorio.MODID, "contract"));

    List<CraftorioContractItem> itemBounty;

    public static final Codec<CraftorioContract> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(CraftorioContractItem.CODEC).fieldOf("itemBounty").forGetter(CraftorioContract::getItemBounty),
                    Codec.STRING.fieldOf("name").forGetter(CraftorioContract::getActualName),
                    Codec.STRING.fieldOf("description").forGetter(CraftorioContract::getDescription),
                    Codec.INT.fieldOf("seconds").forGetter(contract -> contract.getTime() / SECONDS_TO_TICKS),
                    BIGINT_CODEC().optionalFieldOf("basePointValue", BigInteger.ZERO).forGetter(CraftorioContract::getBasePointValue),
                    Codec.list(CraftorioContractItemReward.CODEC).optionalFieldOf("itemRewards", List.of()).forGetter(CraftorioContract::getRewards),
                    ResourceLocation.CODEC.optionalFieldOf("punishment").forGetter(contract -> Optional.ofNullable(contract.getPunishment())),
                    Codec.INT.fieldOf("weight").forGetter(CraftorioContract::getWeight),
                    SCIENTIFIC_BIGINT_CODEC().optionalFieldOf("point_threshold", BigInteger.ZERO).forGetter(CraftorioContract::getPointThreshold),
                    SCIENTIFIC_BIGINT_CODEC().optionalFieldOf("min_point_threshold", BigInteger.ZERO).forGetter(CraftorioContract::getMinPointThreshold),
                    SCIENTIFIC_BIGINT_CODEC().optionalFieldOf("max_point_threshold", CraftorioMisc.pointThreshold()).forGetter(CraftorioContract::getMaxPointThreshold),
                    Codec.STRING.optionalFieldOf("required_mod_id").forGetter(contract -> Optional.ofNullable(contract.getRequiredModId())),
                    ResourceKey.codec(CraftorioContractTexture.REGISTRY_KEY).optionalFieldOf("card_texture").forGetter(contract -> Optional.ofNullable(contract.getCardTexture())),
                    ContractTextColors.CODEC.optionalFieldOf("text_colors", ContractTextColors.EMPTY).forGetter(CraftorioContract::getTextColors)
            ).apply(instance, (itemBounty, name, description, seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, cardTexture, textColors) ->
                    new CraftorioContract(itemBounty, name, description, seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, cardTexture, textColors))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftorioContract> CODEC_STREAM = StreamCodec.of(
            (buffer, contract) -> {
                CraftorioContractItem.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, contract.getItemBounty());
                ByteBufCodecs.STRING_UTF8.encode(buffer, contract.getActualName());
                ByteBufCodecs.STRING_UTF8.encode(buffer, contract.getDescription());
                ByteBufCodecs.INT.encode(buffer, contract.getTime());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getBasePointValue());
                CraftorioContractItemReward.CODEC_STREAM.apply(ByteBufCodecs.list()).encode(buffer, contract.getRewards());
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buffer, Optional.ofNullable(contract.getPunishment()));
                ByteBufCodecs.BOOL.encode(buffer, contract.isAbandoned());
                ByteBufCodecs.INT.encode(buffer, contract.getWeight());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getPointThreshold());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getMinPointThreshold());
                ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).encode(buffer, contract.getMaxPointThreshold());
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buffer, Optional.ofNullable(contract.getRequiredModId()));
                ByteBufCodecs.optional(ResourceKey.streamCodec(CraftorioContractTexture.REGISTRY_KEY)).encode(buffer, Optional.ofNullable(contract.getCardTexture()));
                ContractTextColors.STREAM_CODEC.encode(buffer, contract.getTextColors());
            },
            buffer -> {
                List<CraftorioContractItem> itemBounty = CraftorioContractItem.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                String name = ByteBufCodecs.STRING_UTF8.decode(buffer);
                String description = ByteBufCodecs.STRING_UTF8.decode(buffer);
                int time = ByteBufCodecs.INT.decode(buffer);
                BigInteger basePointValue = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                List<CraftorioContractItemReward> rewards = CraftorioContractItemReward.CODEC_STREAM.apply(ByteBufCodecs.list()).decode(buffer);
                Optional<ResourceLocation> punishment = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buffer);
                boolean abandoned = ByteBufCodecs.BOOL.decode(buffer);
                int weight = ByteBufCodecs.INT.decode(buffer);
                BigInteger pointThreshold = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger minPointThreshold = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                BigInteger maxPointThreshold = ByteBufCodecs.fromCodec(CraftorioMisc.BIGINT_CODEC()).decode(buffer);
                Optional<String> requiredModId = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buffer);
                Optional<ResourceKey<CraftorioContractTexture>> cardTexture = ByteBufCodecs.optional(ResourceKey.streamCodec(CraftorioContractTexture.REGISTRY_KEY)).decode(buffer);
                ContractTextColors textColors = ContractTextColors.STREAM_CODEC.decode(buffer);
                CraftorioContract contract = new CraftorioContract(itemBounty, name, description, time / SECONDS_TO_TICKS, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, cardTexture, textColors);
                contract.setTime(time);
                return contract;
            }
    );

    public CraftorioContract(List<CraftorioContractItem> itemBounty, String name, String description, int seconds, BigInteger basePointValue, List<CraftorioContractItemReward> rewards, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold, Optional<String> requiredModId){
        this(itemBounty, name, description, seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, Optional.empty());
    }

    public CraftorioContract(List<CraftorioContractItem> itemBounty, String name, String description, int seconds, BigInteger basePointValue, List<CraftorioContractItemReward> rewards, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold, Optional<String> requiredModId, Optional<ResourceKey<CraftorioContractTexture>> cardTexture){
        this(itemBounty, name, description, seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, cardTexture, ContractTextColors.EMPTY);
    }

    public CraftorioContract(List<CraftorioContractItem> itemBounty, String name, String description, int seconds, BigInteger basePointValue, List<CraftorioContractItemReward> rewards, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold, Optional<String> requiredModId, Optional<ResourceKey<CraftorioContractTexture>> cardTexture, ContractTextColors textColors){
        this.itemBounty = itemBounty;
        this.name = name;
        this.description = description;
        this.time = seconds * SECONDS_TO_TICKS;
        this.basePointValue = basePointValue != null ? basePointValue : BigInteger.ZERO;
        this.rewards = rewards != null ? rewards : List.of();
        this.punishment = punishment.orElse(null);
        this.abandoned = false;
        this.weight = weight;
        this.pointThreshold = pointThreshold;
        this.minPointThreshold = minPointThreshold != null ? minPointThreshold : BigInteger.ZERO;
        this.maxPointThreshold = maxPointThreshold != null ? maxPointThreshold : CraftorioMisc.pointThreshold();
        this.requiredModId = requiredModId != null ? requiredModId.orElse(null) : null;
        this.cardTexture = cardTexture != null ? cardTexture.orElse(null) : null;
        this.textColors = textColors != null ? textColors : ContractTextColors.EMPTY;
    }

    public CraftorioContract(List<CraftorioContractItem> itemBounty, String langKey, int seconds, BigInteger basePointValue, List<CraftorioContractItemReward> rewards, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold, Optional<String> requiredModId){
        this(itemBounty, "registry." + langKey + ".title", "registry." + langKey + ".description", seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId);
    }

    public CraftorioContract(List<CraftorioContractItem> itemBounty, String langKey, int seconds, BigInteger basePointValue, List<CraftorioContractItemReward> rewards, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold, Optional<String> requiredModId, Optional<ResourceKey<CraftorioContractTexture>> cardTexture){
        this(itemBounty, "registry." + langKey + ".title", "registry." + langKey + ".description", seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, cardTexture);
    }

    public CraftorioContract(List<CraftorioContractItem> itemBounty, String langKey, int seconds, BigInteger basePointValue, List<CraftorioContractItemReward> rewards, Optional<ResourceLocation> punishment, int weight, BigInteger pointThreshold, BigInteger minPointThreshold, BigInteger maxPointThreshold, Optional<String> requiredModId, Optional<ResourceKey<CraftorioContractTexture>> cardTexture, ContractTextColors textColors){
        this(itemBounty, "registry." + langKey + ".title", "registry." + langKey + ".description", seconds, basePointValue, rewards, punishment, weight, pointThreshold, minPointThreshold, maxPointThreshold, requiredModId, cardTexture, textColors);
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

    public CraftorioContract copy() {
        List<CraftorioContractItem> copiedItems = new ArrayList<>();
        for (CraftorioContractItem item : itemBounty) {
            copiedItems.add(item.copy());
        }
        CraftorioContract copy = new CraftorioContract(copiedItems, getActualName(), getDescription(), getTime() / SECONDS_TO_TICKS, getBasePointValue(), getRewards(), Optional.ofNullable(getPunishment()), getWeight(), getPointThreshold(), getMinPointThreshold(), getMaxPointThreshold(), Optional.ofNullable(getRequiredModId()), Optional.ofNullable(getCardTexture()), getTextColors());
        copy.setTime(getTime());
        return copy;
    }

    public boolean shouldEnd(){
        return isComplete() || time <= 0;
    }

    public void addSinkedListValue(List<ItemStack> itemsSinked, Player player){
        for (ItemStack itemStack : itemsSinked){
            for (CraftorioContractItem contractItem : getItemBounty()){
                if (!contractItem.isComplete() && contractItem.matches(itemStack)){
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

    public BigInteger getBasePointValue() {
        return basePointValue;
    }

    public void setBasePointValue(BigInteger basePointValue) {
        this.basePointValue = basePointValue;
    }

    public BigInteger getMultipliedPointValue(Player player) {
        float multiplier = CraftorioMisc.getCraftorioMultiplier(player);
        BigDecimal result = new BigDecimal(basePointValue).multiply(BigDecimal.valueOf(multiplier));
        return basePointValue.add(result.toBigInteger());
    }

    public List<CraftorioContractItemReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<CraftorioContractItemReward> rewards) {
        this.rewards = rewards;
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

    public String getRequiredModId() {
        return requiredModId;
    }

    public void setRequiredModId(String requiredModId) {
        this.requiredModId = requiredModId;
    }

    public ResourceKey<CraftorioContractTexture> getCardTexture() {
        return cardTexture;
    }

    public void setCardTexture(ResourceKey<CraftorioContractTexture> cardTexture) {
        this.cardTexture = cardTexture;
    }

    public ContractTextColors getTextColors() {
        return textColors;
    }

    public void setTextColors(ContractTextColors textColors) {
        this.textColors = textColors != null ? textColors : ContractTextColors.EMPTY;
    }

    public ResourceLocation resolveCardTexture(RegistryAccess registryAccess) {
        ResourceLocation fallback = Craftorio.getGuiTexture("contract_textures/default_contract.png");
        if (cardTexture == null) return fallback;

        return registryAccess.registryOrThrow(CraftorioContractTexture.REGISTRY_KEY)
                .getOptional(cardTexture)
                .map(CraftorioContractTexture::texture)
                .orElse(fallback);
    }

    public boolean isModAvailable() {
        return requiredModId == null || ModList.get().isLoaded(requiredModId);
    }

    public void abandon() {
        this.abandoned = true;
        setTime(0);
    }

    public void forceComplete(Player player){
        for (CraftorioContractItem contractItem : itemBounty){
            contractItem.setItemsGiven(contractItem.getAmountRequired());
        }

        CraftorioMisc.setPoints(getMultipliedPointValue(player).add(CraftorioMisc.getPoints(player)),player);
        CraftorioMisc.recordContractCompleted(player);
        for (CraftorioContractItemReward itemReward : getRewards()){
            itemReward.giveItems(player);
        }

        List<CraftorioContract> newContract = new ArrayList<>(CraftorioMisc.getCraftorioContracts(player));
        newContract.remove(this);
        CraftorioMisc.setCraftorioContracts(player,newContract);
    }

    public void tick(Player player){
        setTime(time - 1);
        if (shouldEnd()){
            if (isComplete()){
                CraftorioMisc.setPoints(getMultipliedPointValue(player).add(CraftorioMisc.getPoints(player)),player);
                CraftorioMisc.recordContractCompleted(player);
                for (CraftorioContractItemReward itemReward : getRewards()){
                    itemReward.giveItems(player);
                }
            } else {
                setTime(0);
                if (getPunishment() != null) {
                    CraftorioMisc.punishContractFailure(player, this, getPunishment());
                }
            }

            List<CraftorioContract> newContract = new ArrayList<>(CraftorioMisc.getCraftorioContracts(player));
            newContract.remove(this);
            CraftorioMisc.setCraftorioContracts(player,newContract);
        }
    }

}
