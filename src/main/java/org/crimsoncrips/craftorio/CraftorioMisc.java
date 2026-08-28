package org.crimsoncrips.craftorio;

import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
        int sizePicked = Craftorio.SERVER_CONFIG.STARTING_LAND_SIZE.getAsInt();
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
    public static void ownChunk(List<ChunkPos> chunkPos, Level level, boolean claiming){
        if (level == null) return;
        long claimed_amount = getLandAmount(level);
        BigInteger points = getPoints(level);
        BigInteger amountToClaim = CraftorioMisc.pointsToExpand(chunkPos.size(),claimed_amount);
        if (points.compareTo(amountToClaim) >= 0){
            for (ChunkPos chunkSelected : chunkPos) {
                ChunkAccess chunk = level.getChunk(chunkSelected.x, chunkSelected.z);

                if ((!startingLocations().contains(chunkSelected))) {
                    if (claiming && (!isOwned(chunk))) {
                        setOwned(chunk,true);
                        setLandAmount(level,claimed_amount + 1);
                        setPoints(level,points.subtract(amountToClaim));
                    } else if ((!claiming) && CraftorioMisc.isOwned(chunk)) {
                        setOwned(chunk,false);
                        setLandAmount(level,claimed_amount - 1);
                    }
                }
            }
        }
    }

    public static void expandBorder(long expandAmount, Level level, boolean expand){
        if (level == null) return;
        long claimed_amount = getLandAmount(level);
        BigInteger points = getPoints(level);
        BigInteger amountToClaim = CraftorioMisc.pointsToExpand(expandAmount,claimed_amount);
        expandAmount *= Craftorio.SERVER_CONFIG.EXPANSION_AMOUNT.getAsInt();
        if (points.compareTo(amountToClaim) >= 0){
            System.out.println(expandAmount);
            double borderSize = level.getWorldBorder().getSize();
            if (expand){
                level.getWorldBorder().lerpSizeBetween(borderSize,borderSize + expandAmount,1000);
                setPoints(level, points.subtract(amountToClaim));
                setLandAmount(level,getLandAmount(level) + expandAmount);
            } else {
                level.getWorldBorder().lerpSizeBetween(borderSize,borderSize - expandAmount,1000);
            }
        }
    }

    public static boolean chunkBased(Level level){
        return level.getData(CHUNK_BASED);
    }



    public static BigInteger checkValue(ItemStack itemStack){
        var valueString = itemStack.getItem().builtInRegistryHolder().getData(CraftorioDataMaps.POINT_VALUE);
        BigDecimal determinedValue;
        determinedValue = valueString != null ? (new BigDecimal(valueString).multiply(BigDecimal.valueOf(itemStack.getCount()))) : BigDecimal.valueOf(0);
        BigInteger value = determinedValue.toBigInteger();
        //MobEffect Check
        var effect = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        for (MobEffectInstance mobEffect : effect.getAllEffects()) {
            var effectValue = mobEffect.getEffect().getData(CraftorioDataMaps.EFFECT_POINT_VALUE);
            if (effectValue != null) {
                int multiplier = mobEffect.getAmplifier() > 1 ? mobEffect.getAmplifier() - 1 : 0;
                value = value.add((new BigInteger(effectValue).multiply(BigInteger.valueOf((long) (1 + (multiplier * 0.45))))));
            }
        }


        //Enchanted Book Check
        ItemEnchantments storedEnchant = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        for (Object2IntMap.Entry<Holder<Enchantment>> pickedEnchant : storedEnchant.entrySet()){
            int level = storedEnchant.getLevel(pickedEnchant.getKey());

            var enchant = pickedEnchant.getKey().getData(CraftorioDataMaps.ENCHANTMENT_POINT_VALUE);
            if (enchant != null) {
                int multiplier = level > 1 ? level - 1 : 0;
                value = value.add((new BigInteger(enchant).multiply(BigInteger.valueOf((long) (1 + (multiplier * 0.45))))));
            }
        }

        //Enchanted Item Check
        ItemEnchantments enchants = itemStack.getEnchantments();
        for (Object2IntMap.Entry<Holder<Enchantment>> pickedEnchant : enchants.entrySet()){
            int level = EnchantmentHelper.getItemEnchantmentLevel(pickedEnchant.getKey(),itemStack);


            var enchant = pickedEnchant.getKey().getData(CraftorioDataMaps.ENCHANTMENT_POINT_VALUE);
            if (enchant != null) {
                int multiplier = level > 1 ? level - 1 : 0;
                value = value.add((new BigInteger(enchant).multiply(BigInteger.valueOf((long) (1 + (multiplier * 0.45))))));
            }
        }

        return value;
    }

    public static BigInteger pointsToExpand(long amount, long claimedLand) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal baseCost = BigDecimal.valueOf(landBaseCost());
        for (int i = 0; i < amount; i++) {

            BigDecimal cost = baseCost.multiply(BigDecimal.valueOf(Math.pow(landCostIncreaser(), claimedLand + i)));
            total = total.add(cost);
        }
        return total.setScale(0, RoundingMode.CEILING).toBigInteger();
    }

    public static long expandCapabilityWithPoints(BigInteger points, long claimedLand) {
        BigDecimal baseCost = BigDecimal.valueOf(landBaseCost());
        BigInteger remaining = points;
        long claimable = 0;
        long currentLand = claimedLand;

        while (true) {
            BigDecimal costDecimal = baseCost.multiply(BigDecimal.valueOf(Math.pow(landCostIncreaser(), currentLand)));
            BigInteger cost = costDecimal.setScale(0, RoundingMode.CEILING).toBigInteger();

            if (cost.compareTo(remaining) > 0) break;

            remaining = remaining.subtract(cost);
            claimable++;
            currentLand++;
        }

        return claimable;
    }


    public static float landCostIncreaser() {
        return 1 + (float) Craftorio.SERVER_CONFIG.COST_MULTIPLIER.getAsDouble();
    }

    public static long landBaseCost() {
        return Craftorio.SERVER_CONFIG.COST_BASE.getAsInt();
    }

    public static BigInteger toBigInteger(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input string is null or empty");
        }

        // strip whitespace and common formatting (commas)
        String cleaned = input.trim().replace(",", "");

        BigDecimal decimal;
        try {
            decimal = new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input: " + input, e);
        }

        return decimal.setScale(0, RoundingMode.HALF_UP).toBigInteger();
    }


    //Points
    public static BigInteger getPoints(Level level){
        return level.getData(POINTS);
    }
    
    public static void setPoints(Level level,BigInteger points){
        level.setData(POINTS,points);
    }

    public static void addPoints(Level level,BigInteger addition){
        setPoints(level,addition.add(getPoints(level)));
    }

    public static void subtractPoints(Level level,BigInteger addition){
        setPoints(level,addition.subtract(getPoints(level)));
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
    public static long getLandAmount(Level level){
        return level.getData(AMOUNT_OF_LAND);
    }
    
    public static void setLandAmount(Level level,long amount){
        level.setData(AMOUNT_OF_LAND,amount);
    }

    private static final String[] SUFFIXES = {
            "k","M","B","T","qd","Qn","sx","Sp","O","N",
            "de","Ud","DD","tdD","qdD","QnD","sxD","SpD","OcD","NvD",
            "Vgn","UVg","DVg","TVg","qtV","QnV","SeV","SPG","OVG","NVG",
            "TGN","UTG","DTG","tsTG","qtTG","QnTG","ssTG","SpTG","OcTG","NoTG",
            "QdDR","uQDR","dQDR","tQDR","qdQDR","QnQDR","sxQDR","SpQDR","OQDDr","NQDDr",
            "qQGNT","uQGNT","dQGNT","tQGNT","qdQGNT","QnQGNT","sxQGNT","SpQGNT","OQQGNT","NQQGNT",
            "SXGNTL","USXGNTL","DSXGNTL","TSXGNTL","QTSXGNTL","QNSXGNTL","SXSXGNTL","SPSXGNTL","OSXGNTL","NVSXGNTL",
            "SPTGNTL","USPTGNTL","DSPTGNTL","TSPTGNTL","QTSPTGNTL","QNSPTGNTL","SXSPTGNTL","SPSPTGNTL","OSPTGNTL","NVSPTGNTL",
            "OTGNTL","UOTGNTL","DOTGNTL","TOTGNTL","QTOTGNTL","QNOTGNTL","SXOTGNTL","SPOTGNTL","OTOTGNTL","NVOTGNTL",
            "NONGNTL","UNONGNTL","DNONGNTL","TNONGNTL","QTNONGNTL","QNNONGNTL","SXNONGNTL","SPNONGNTL","OTNONGNTL","NONONGNTL",
            "CENT","UNCENT"
    };

    private static final String[] NAMES = {
            "Thousand","Million","Billion","Trillion","Quadrillion","Quintillion","Sextillion","Septillion","Octillion","Nonillion",
            "Decillion","Undecillion","Duodecillion","Tredecillion","Quattuordecillion","Quindecillion","Sedecillion","Septendecillion","Octodecillion","Novemdecillion",
            "Vigintillion","Unvigintillion","Duovigintillion","Tresvigintillion","Quattuorvigintillion","Quinvigintillion","Sesvigintillion","Septemvigintillion","Octovigintillion","Novemvigintillion",
            "Trigintillion","Untrigintillion","Duotrigintillion","Trestrigintillion","Quattuortrigintillion","Quintrigintillion","Sestrigintillion","Septentrigintillion","Octotrigintillion","Novemtrigintillion",
            "Quadragintillion","Unquadragintillion","Duoquadragintillion","Tresquadragintillion","Quattuorquadragintillion","Quinquadragintillion","Sesquadragintillion","Septenquadragintillion","Octoquadragintillion","Novemquadragintillion",
            "Quinquagintillion","Unquinquagintillion","Duoquinquagintillion","Tresquinquagintillion","Quattuorquinquagintillion","Quinquinquagintillion","Sesquinquagintillion","Septenquinquagintillion","Octoquinquagintillion","Novemquinquagintillion",
            "Sexagintillion","Unsexagintillion","Duosexagintillion","Tresexagintillion","Quattuorsexagintillion","Quinsexagintillion","Sesexagintillion","Septensexagintillion","Octosexagintillion","Novemsexagintillion",
            "Septuagintillion","Unseptuagintillion","Duoseptuagintillion","Treseptuagintillion","Quattuorseptuagintillion","Quinseptuagintillion","Seseptuagintillion","Septenseptuagintillion","Octoseptuagintillion","Novemseptuagintillion",
            "Octogintillion","Unoctogintillion","Duooctogintillion","Treoctogintillion","Quattuoroctogintillion","Quinoctogintillion","Sexoctogintillion","Septemoctogintillion","Octooctogintillion","Novemoctogintillion",
            "Nonagintillion","Unnonagintillion","Duononagintillion","Trenonagintillion","Quattuornonagintillion","Quinnonagintillion","Senonagintillion","Septenonagintillion","Octononagintillion","Novemnonagintillion",
            "Centillion","Uncentillion"
    };


    public static String bigIntFormat(BigInteger value, int formatType) {
        if (value.signum() == 0) {
            return "0";
        }

        boolean negative = value.signum() < 0;
        BigInteger absValue = value.abs();

        String result = switch (formatType) {
            case 0 -> value.toString();
            case 1 -> toScientificNotation(absValue);
            case 2 -> toSuffix(absValue,false);
            case 3 -> toSuffix(absValue, true);
            default -> throw new IllegalArgumentException(
                    "Invalid formatType: " + formatType + " (use 1 for scientific, 2 for suffix)");
        };

        return negative ? "-" + result : result;
    }

    private static String toScientificNotation(BigInteger absValue) {
        int digitCount = absValue.toString().length();
        int exponent = digitCount - 1;

        BigDecimal mantissa = new BigDecimal(absValue)
                .movePointLeft(exponent)
                .setScale(2, RoundingMode.DOWN)
                .stripTrailingZeros();

        String mantissaStr = mantissa.toPlainString();
        return mantissaStr + "e" + exponent;
    }

    private static String toSuffix(BigInteger absValue, boolean worded) {
        int digitCount = absValue.toString().length();
        int exponent = digitCount - 1;

        if (exponent < 3) {
            return absValue.toString();
        }

        int suffixIndex = exponent / 3;
        if (suffixIndex > NAMES.length) {
            return toScientificNotation(absValue);
        }
        int groupExponent = suffixIndex * 3;
        BigDecimal divisor = BigDecimal.TEN.pow(groupExponent);
        BigDecimal scaled = new BigDecimal(absValue)
                .divide(divisor, 2, RoundingMode.DOWN)
                .stripTrailingZeros();
        if (!worded){
            String suffix = SUFFIXES[suffixIndex - 1];
            return scaled.toPlainString() + suffix;
        } else {
            String suffix = NAMES[suffixIndex - 1];
            return scaled.toPlainString() + " " + suffix;
        }

    }


}
