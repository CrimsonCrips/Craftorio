package org.crimsoncrips.craftorio;

import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;

import java.util.ArrayList;
import java.util.List;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.*;

public class CraftorioMisc {

    public static List<ChunkPos> generateSelectionChunks(int startX,int startZ, int endX, int endZ) {

        List<ChunkPos> chunks = new ArrayList<>();

        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        int minY = Math.min(startZ, endZ);
        int maxY = Math.max(startZ, endZ);


        for (int y = minY; y <= maxY; y++) {

            for (int x = minX; x <= maxX; x++) {
                chunks.add(new ChunkPos(x,y));
            }
        }
        return chunks;
    }

    public static List<ChunkPos> startingLocations(){
        int sizePicked = Craftorio.COMMON_CONFIG.STARTING_LAND_SIZE.getAsInt();
        ChunkPos startPos = new ChunkPos(-sizePicked,-sizePicked);
        ChunkPos endPos = new ChunkPos(sizePicked,sizePicked);

        List<ChunkPos> chunkCoords = new ArrayList<>();
        int minX = Math.min(startPos.x, endPos.x);
        int maxX = Math.max(startPos.x, endPos.x);
        int minY = Math.min(startPos.z, endPos.z);
        int maxY = Math.max(startPos.z, endPos.z);


        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                chunkCoords.add(new ChunkPos(x,y));
            }
        }
        return chunkCoords;
    }
    public static void ownLand(List<ChunkPos> chunkPos,Level level, boolean claiming){
        if (level == null) return;
        int claimed_amount = getLandAmount(level);
        long points = getPoints(level);
        long amountToClaim = CraftorioMisc.pointsToClaimLand(chunkPos.size(),claimed_amount);
        if (points >= amountToClaim){
            for (ChunkPos chunkSelected : chunkPos) {
                ChunkAccess chunk = level.getChunk(chunkSelected.x, chunkSelected.z);

                if ((!startingLocations().contains(chunkSelected))) {
                    if (claiming && (!isOwned(chunk))) {
                        setOwned(chunk,true);
                        setLandAmount(level,claimed_amount + 1);
                    } else if ((!claiming) && chunk.hasData(OWNED)) {
                        setOwned(chunk,false);
                        setLandAmount(level,claimed_amount - 1);
                    }
                }
            }
            level.setData(POINTS,points - amountToClaim);
        }
    }

    public static boolean chunkBased(Level level){
        return level.getData(CHUNK_BASED);
    }



    public static int checkValue(ItemStack itemStack){
        var value = itemStack.getItem().builtInRegistryHolder().getData(CraftorioDataMaps.POINT_VALUE);
        value = value != null ? value * itemStack.getCount() : 0;

        //MobEffect Check
        var effect = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        for (MobEffectInstance mobEffect : effect.getAllEffects()) {
            var effectValue = mobEffect.getEffect().getData(CraftorioDataMaps.EFFECT_POINT_VALUE);
            if (effectValue != null) {
                int multiplier = mobEffect.getAmplifier() > 1 ? mobEffect.getAmplifier() - 1 : 0;
                value = value + Math.toIntExact((long) (effectValue * (1 + (multiplier * 0.45))));
            }
        }


        //Enchanted Book Check
        ItemEnchantments storedEnchant = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        for (Object2IntMap.Entry<Holder<Enchantment>> pickedEnchant : storedEnchant.entrySet()){
            int level = storedEnchant.getLevel(pickedEnchant.getKey());

            var enchant = pickedEnchant.getKey().getData(CraftorioDataMaps.ENCHANTMENT_POINT_VALUE);
            if (enchant != null) {
                int multiplier = level > 1 ? level - 1 : 0;
                value = value + Math.toIntExact((long) (enchant * (1 + (multiplier * 0.45))));
            }
        }

        //Enchanted Item Check
        ItemEnchantments enchants = itemStack.getEnchantments();
        for (Object2IntMap.Entry<Holder<Enchantment>> pickedEnchant : enchants.entrySet()){
            int level = EnchantmentHelper.getItemEnchantmentLevel(pickedEnchant.getKey(),itemStack);


            var enchant = pickedEnchant.getKey().getData(CraftorioDataMaps.ENCHANTMENT_POINT_VALUE);
            if (enchant != null) {
                int multiplier = level > 1 ? level - 1 : 0;
                value = value + Math.toIntExact((long) (enchant * (1 + (multiplier * 0.45))));
            }
        }

        return value;
    }

    public static long pointsToClaimLand(int amount,int claimedLand) {
        if (amount <= 0) return 0L;

        double total = 0.0;
        for (int i = 0; i < amount; i++) {
            total += landBaseCost() * Math.pow(landCostIncreaser(), claimedLand + i);
        }
        return (long) Math.ceil(total);
    }

    public static int landClaimableWithPoints(long points,int claimedLand) {
        long remaining = points;
        int claimable = 0;
        int currentLand = claimedLand;

        while (true) {
            long cost = (long) Math.ceil(landBaseCost() * Math.pow(landCostIncreaser(), currentLand));
            if (cost > remaining) break;
            remaining -= cost;
            claimable++;
            currentLand++;
        }

        return claimable;
    }


    public static float landCostIncreaser() {
        return 1.2F;
    }

    public static long landBaseCost() {
        return 10L;
    }


    //Points
    public static long getPoints(Level level){
        return level.getData(POINTS);
    }
    
    public static void setPoints(Level level,long points){
        level.setData(POINTS,points);
    }

    public static void addPoints(Level level,long addition){
        level.setData(POINTS,getPoints(level) + addition);
    }

    //Owned
    public static boolean isOwned(ChunkAccess chunkAccess){
        return chunkAccess.hasData(OWNED);
    }

    public static void setOwned(ChunkAccess chunkAccess,boolean own){
        if (own){
            chunkAccess.setData(OWNED,Unit.INSTANCE);
        } else {
            chunkAccess.removeData(OWNED);
        }
    }

    
    //Land
    public static int getLandAmount(Level level){
        return level.getData(AMOUNT_OF_LAND);
    }
    
    public static void setLandAmount(Level level,int amount){
        level.setData(AMOUNT_OF_LAND,amount);
    }





}
