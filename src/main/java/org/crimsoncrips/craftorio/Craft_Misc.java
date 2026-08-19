package org.crimsoncrips.craftorio;

import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.crimsoncrips.craftorio.datagen.maps.ModDataMaps;

import java.util.ArrayList;
import java.util.List;

import static org.crimsoncrips.craftorio.Craftorio.*;

public class Craft_Misc {

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
        ChunkPos startPos = new ChunkPos(-1,-1);
        ChunkPos endPos = new ChunkPos(1,1);

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
    public static void ownLand(ChunkPos chunkPos,Level level, boolean claiming){
        if (level == null) return;
        int claimed_amount = level.getData(AMOUNT_OF_LAND);
        ChunkAccess chunk = level.getChunk(chunkPos.x,chunkPos.z);

        if((!startingLocations().contains(chunkPos))){
            if (claiming && (!chunk.hasData(OWNED))) {
                chunk.setData(OWNED, Unit.INSTANCE);
                int amountSet = claimed_amount + 1;
                level.setData(AMOUNT_OF_LAND, amountSet);
            } else if ((!claiming) && chunk.hasData(OWNED)) {
                chunk.removeData(OWNED);
                int amountSet = claimed_amount - 1;
                level.setData(AMOUNT_OF_LAND,amountSet);
            }
        }
    }

    public static int checkValue(ItemStack itemStack){
        var value = itemStack.getItem().builtInRegistryHolder().getData(ModDataMaps.POINT_VALUE);
        value = value != null ? value * itemStack.getCount() : 0;

        //MobEffect Check
        var effect = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        for (MobEffectInstance mobEffect : effect.getAllEffects()) {
            var effectValue = mobEffect.getEffect().getData(ModDataMaps.EFFECT_POINT_VALUE);
            if (effectValue != null) {
                int multiplier = mobEffect.getAmplifier() > 1 ? mobEffect.getAmplifier() - 1 : 0;
                value = value + Math.toIntExact((long) (effectValue * (1 + (multiplier * 0.45))));
            }
        }


        //Enchanted Book Check
        ItemEnchantments storedEnchant = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        for (Object2IntMap.Entry<Holder<Enchantment>> pickedEnchant : storedEnchant.entrySet()){
            int level = storedEnchant.getLevel(pickedEnchant.getKey());

            var enchant = pickedEnchant.getKey().getData(ModDataMaps.ENCHANTMENT_POINT_VALUE);
            if (enchant != null) {
                int multiplier = level > 1 ? level - 1 : 0;
                value = value + Math.toIntExact((long) (enchant * (1 + (multiplier * 0.45))));
            }
        }

        //Enchanted Item Check
        ItemEnchantments enchants = itemStack.getEnchantments();
        for (Object2IntMap.Entry<Holder<Enchantment>> pickedEnchant : enchants.entrySet()){
            int level = EnchantmentHelper.getItemEnchantmentLevel(pickedEnchant.getKey(),itemStack);


            var enchant = pickedEnchant.getKey().getData(ModDataMaps.ENCHANTMENT_POINT_VALUE);
            if (enchant != null) {
                int multiplier = level > 1 ? level - 1 : 0;
                value = value + Math.toIntExact((long) (enchant * (1 + (multiplier * 0.45))));
            }
        }

        return value;
    }

    public static long calculateLandCost(int claimAmount,Level level){
        int currentLand = level.getData(AMOUNT_OF_LAND);
        return (currentLand + claimAmount) > 1 ? ((currentLand + claimAmount) * 10L) : 10L;
    }



}
