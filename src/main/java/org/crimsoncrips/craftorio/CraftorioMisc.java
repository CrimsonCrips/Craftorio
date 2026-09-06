package org.crimsoncrips.craftorio;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import org.crimsoncrips.craftorio.registries.contracts.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.registries.contracts.shipment.CraftorioShipmentItem;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;
import org.crimsoncrips.craftorio.registries.effect.CraftorioPointEffect;
import org.crimsoncrips.craftorio.registries.effect.GeneralMultiplierEffect;
import org.crimsoncrips.craftorio.registries.effect.TagMultiplierEffect;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    public static List<ChunkPos> startingLocations(ChunkPos chunkPos){
        int sizePicked = startingLand();
        ChunkPos startPos = new ChunkPos(chunkPos.x - sizePicked,chunkPos.z - sizePicked);
        ChunkPos endPos = new ChunkPos(chunkPos.x + sizePicked,chunkPos.z + sizePicked);

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
    public static void ownChunk(List<ChunkPos> chunkPos, Level level, boolean claiming, Player player,boolean starting){
        if (level == null) return;

        if (starting) {
            for (ChunkPos chunkSelected : chunkPos) {
                ChunkAccess chunk = level.getChunk(chunkSelected.x, chunkSelected.z);
                if (claiming && !isOwnedBy(chunk, player)) {
                    setOwnedBy(chunk, player, true);
                } else if (!claiming && isOwnedBy(chunk, player)) {
                    setOwnedBy(chunk, player, false);
                }
            }
        } else {
            long claimed_amount = getLandAmount(player);
            BigInteger points = getPoints(player);
            BigInteger amountToClaim = CraftorioMisc.pointsToExpand(chunkPos.size(),claimed_amount);
            if (!(points.compareTo(amountToClaim) >= 0))
                return;

            for (ChunkPos chunkSelected : chunkPos) {
                ChunkAccess chunk = level.getChunk(chunkSelected.x, chunkSelected.z);
                if (startingLocations(level.getChunk(CraftorioMisc.getPlayerOrigin(player)).getPos()).contains(chunkSelected))
                    return;
                if (claiming && !isOwnedBy(chunk, player)) {
                    setOwnedBy(chunk, player, true);
                    setLandAmount(claimed_amount + 1, player);
                    setPoints(points.subtract(amountToClaim), player);
                } else if (!claiming && isOwnedBy(chunk, player)) {
                    setOwnedBy(chunk, player, false);
                    setLandAmount(claimed_amount - 1, player);
                }
            }
        }
    }

    public static void expandBorder(long expandAmount, Level level, boolean expand,Player player){
        if (level == null) return;
        long claimed_amount = getLandAmount(player);
        BigInteger points = getPoints(player);
        BigInteger amountToClaim = CraftorioMisc.pointsToExpand(expandAmount,claimed_amount);
        expandAmount *= Craftorio.SERVER_CONFIG.EXPANSION_AMOUNT.getAsInt();
        if (points.compareTo(amountToClaim) >= 0){
            System.out.println(expandAmount);
            double borderSize = level.getWorldBorder().getSize();
            if (expand){
                level.getWorldBorder().lerpSizeBetween(borderSize,borderSize + expandAmount,3000);
                setPoints(points.subtract(amountToClaim),player);
                setLandAmount(getLandAmount(player) + expandAmount,player);
            } else {
                level.getWorldBorder().lerpSizeBetween(borderSize,borderSize - expandAmount,3000);
            }
        }
    }

    public static boolean chunkBased(Level level){
        return level.getData(CHUNK_BASED);
    }

    public static boolean universalBased(Level level){
        return level.getData(UNIVERSAL_BASED);
    }



    public static BigInteger checkValue(ItemStack itemStack,Player player,boolean forToolip){
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

        //Buff Multiplier Addition
        if (!forToolip){
            BigDecimal result = new BigDecimal(value).multiply(BigDecimal.valueOf(itemMultiplierValue(player,itemStack)));
            value = value.add(result.toBigInteger());
        }


        return value;
    }

    public static float itemMultiplierValue(Player player, ItemStack itemStack){
        float multiplier = 0;
        for (CraftorioPointEffect craftorioEffect : getCraftorioPointEffects(player)) {
            if (craftorioEffect instanceof TagMultiplierEffect multiplierEffect) {
                multiplier += multiplierEffect.getTagMultiplier(itemStack);
            } else {
                multiplier += craftorioEffect.getMultiplier();
            }
        }
        return multiplier;
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

        BigInteger cap = pointThreshold();

        while (true) {
            double multiplier = Math.pow(landCostIncreaser(), currentLand);

            BigInteger cost;
            if (Double.isInfinite(multiplier) || Double.isNaN(multiplier)) {
                cost = cap;
            } else {
                BigDecimal costDecimal = baseCost.multiply(BigDecimal.valueOf(multiplier));
                cost = costDecimal.setScale(0, RoundingMode.CEILING).toBigInteger();

                if (cost.compareTo(cap) > 0) {
                    cost = cap;
                }
            }

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
        return Craftorio.SERVER_CONFIG.BASE_COST.getAsInt();
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

        BigInteger returnBigInt = decimal.setScale(0, RoundingMode.HALF_UP).toBigInteger();
        if (returnBigInt.compareTo(pointThreshold()) > 0) {
            return pointThreshold();
        } else {
            return returnBigInt;
        }
    }

    public static BigInteger pointThreshold(){
        return new BigDecimal("1e309").toBigInteger();
    }

    //Points
    public static BigInteger getPoints(Player player){
        Level level = player.level();
        if (universalBased(level)){
            return level.getData(POINTS);
        } else {
            return player.getData(POINTS);
        }
    }
    
    public static void setPoints(BigInteger points,Player player){
        Level level = player.level();
        setTempPoints(getPoints(player),player);
        BigInteger assigningPoints = points.compareTo(pointThreshold()) > 0 ? pointThreshold() : points;
        if (universalBased(level)) {
            level.setData(POINTS,assigningPoints);
        } else {
            player.setData(POINTS,assigningPoints);
        }
    }

    //Temporary Points
    public static BigInteger getTempPoints(Player player){
        Level level = player.level();
        if (universalBased(level)){
            return level.getData(TEMP_POINTS);
        } else {
            return player.getData(TEMP_POINTS);
        }
    }


    public static void setTempPoints(BigInteger points,Player player){
        Level level = player.level();
        if (universalBased(level)) {
            level.setData(TEMP_POINTS,points);
        } else {
            player.setData(TEMP_POINTS,points);
        }
    }



    public static BigInteger startingValue(){
        try {
            return new BigDecimal(Craftorio.SERVER_CONFIG.STARTING_POINTS.get()).toBigInteger();
        } catch (Exception e){
            Craftorio.LOGGER.debug("INCORRECT INPUT FOR STARTING_POINTS IN Craftorio Server Config");
            return BigInteger.valueOf(100L);
        }
    }

    public static int startingLand(){
        return Craftorio.SERVER_CONFIG.STARTING_LAND_SIZE.getAsInt();
    }
    //Owned
    public static boolean isOwnedBy(ChunkAccess chunkAccess,Player player){
        Level level = player.level();
        if (isNoBorders(level)) {
            return chunkAccess.hasData(OWNED_BY);
        } else {
            return chunkAccess.getData(OWNED_BY).contains(player.getStringUUID());
        }
    }

    public static void setOwnedBy(ChunkAccess chunkAccess,Player player,boolean own){
        List<String> ownedBy = chunkAccess.getData(OWNED_BY);
        String uuid = player.getStringUUID();

        if (own){
            if (!isOwnedBy(chunkAccess, player)){
                ownedBy.add(uuid);
                chunkAccess.setData(OWNED_BY,ownedBy);
            }
        } else {
            if (isOwnedBy(chunkAccess,player)){
                chunkAccess.getData(OWNED_BY).remove(uuid);
            }
        }
    }

    public static boolean isNoBorders(Level level){
        return level.getData(NO_BORDERS);
    }

    
    //Land
    public static long getLandAmount(Player player){
        Level level = player.level();
        if (universalBased(level)){
            return level.getData(AMOUNT_OF_LAND);
        } else {
            return player.getData(AMOUNT_OF_LAND);
        }
    }
    
    public static void setLandAmount(long amount,Player player){
        Level level = player.level();
        if (universalBased(level)) {
            level.setData(AMOUNT_OF_LAND,amount);
        } else {
            player.setData(AMOUNT_OF_LAND,amount);
        }
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
            "CENT","UNCENT","∞"
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
            "Centillion","Uncentillion","∞ Infinity ∞"
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

        String suffix;
        if (!worded){
            suffix = scaled.toPlainString() + SUFFIXES[suffixIndex - 1];
        } else {
            suffix = scaled.toPlainString() + " " + NAMES[suffixIndex - 1];
        }

        if (suffixIndex > 102){
            suffix = worded ? NAMES[suffixIndex - 1] : SUFFIXES[suffixIndex - 1];
        }
        return suffix;

    }


    public static class CraftorioTextEffects{

        public static void drawFancy(GuiGraphics graphics, Font font, String text,int x, int y, boolean dropShadow, int mode){
            switch (mode) {
                case 0 -> drawRainbowWave(graphics, font, text, x, y, dropShadow);
                case 1 -> drawStaticNoise(graphics, font, text, x, y, dropShadow);
                default -> throw new IllegalArgumentException(
                        "Invalid formatType");
            };
        }

        private static void drawRainbowWave(GuiGraphics graphics, Font font, String text, int x, int y, boolean dropShadow) {
            double time = System.nanoTime() / 1_000_000_000.0;

            float floatX = (float) (Math.sin(time * 0.9) * 2.0);
            float floatY = (float) (Math.cos(time * 1.3) * 1.5);

            int cursorX = x + Math.round(floatX);
            int baseY = y + Math.round(floatY);

            for (int i = 0; i < text.length(); i++) {
                String ch = String.valueOf(text.charAt(i));

                float hue = (float) ((time * 0.25) + (i * 0.08));
                hue -= Math.floor(hue);
                int color = Color.HSBtoRGB(hue, 0.8f, 1.0f) & 0xFFFFFF;

                int charY = baseY + Math.round((float) Math.sin(time * 2.0 + i * 0.6));

                graphics.drawString(font, ch, cursorX, charY, color, dropShadow);
                cursorX += font.width(ch);
            }

        }

        private static void drawStaticNoise(GuiGraphics graphics, Font font, String text, int x, int y, boolean dropShadow) {
            long now = System.currentTimeMillis();
            long frameSeed = now / Math.max(1L, 80L);

            char[] GLITCH_CHARS = {
                    '#', '%', '&', '$', '@', '*', '?', '!', '/', '\\', '|', '~', '^', '0', '1'
            };

            int cursorX = x;

            for (int i = 0; i < text.length(); i++) {
                char original = text.charAt(i);

                long charSeed = frameSeed * 31L + i;
                Random rnd = new Random(charSeed);

                char displayChar = original;
                if (Character.isLetter(original) && rnd.nextFloat() < 0.35f) {
                    displayChar = GLITCH_CHARS[rnd.nextInt(GLITCH_CHARS.length)];
                }

                int gray = 140 + rnd.nextInt(116);
                int color = (gray << 16) | (gray << 8) | gray;

                int jitterX = rnd.nextInt(3) - 1;
                int jitterY = rnd.nextInt(3) - 1;

                graphics.drawString(font, String.valueOf(displayChar), cursorX + jitterX, y + jitterY, color, dropShadow);
                cursorX += font.width(String.valueOf(original));
            }

        }
    }


    //Effect checks
    public static List<CraftorioPointEffect> getCraftorioPointEffects(Player player){
        List<CraftorioPointEffect> newList = new ArrayList<>();
        newList.addAll(getTagEffects(player));
        newList.addAll(getGeneralEffects(player));
        return newList;
    }

    public static List<TagMultiplierEffect> getTagEffects(Player player){
        Level level = player.level();
        if (universalBased(level)){
            return level.getData(TAG_MULTIPLIER_EFFECTS);
        } else {
            return player.getData(TAG_MULTIPLIER_EFFECTS);
        }
    }

    public static List<GeneralMultiplierEffect> getGeneralEffects(Player player){
        Level level = player.level();
        if (universalBased(level)){
            return level.getData(GENERAL_MULTIPLIER_EFFECTS);
        } else {
            return player.getData(GENERAL_MULTIPLIER_EFFECTS);
        }
    }

    public static Optional<CraftorioShipmentContract> getContractTemplate(Level level, ResourceLocation id) {
        Registry<CraftorioShipmentContract> registry = level.registryAccess().registryOrThrow(CraftorioShipmentContract.REGISTRY_KEY);
        return registry.getOptional(id);
    }

    public static void grantContract(Player player, ResourceLocation id) {
        getContractTemplate(player.level(), id).ifPresent(template -> {
            List<CraftorioShipmentContract> playerContract = getCraftorioContracts(player);
            playerContract.add(template.copy());
            setCraftorioContracts(player,playerContract);
        });
    }

    public static void grantEffect(Player player, ResourceLocation id) {
        Level level = player.level();
        boolean universal = universalBased(level);
        Registry<CraftorioEffects> registry = level.registryAccess().registryOrThrow(CraftorioEffects.REGISTRY_KEY);

        registry.getOptional(id).ifPresent(effect -> {
            if (effect instanceof TagMultiplierEffect tagEffect) {
                List<TagMultiplierEffect> list = getTagEffects(player);
                list.add(tagEffect);
                if (universal){
                    level.setData(TAG_MULTIPLIER_EFFECTS, list);
                } else {
                    player.setData(TAG_MULTIPLIER_EFFECTS, list);
                }
            } else if (effect instanceof GeneralMultiplierEffect generalEffect) {
                List<GeneralMultiplierEffect> list = getGeneralEffects(player);
                list.add(generalEffect);
                if (universal){
                    level.setData(GENERAL_MULTIPLIER_EFFECTS, list);
                } else {
                    player.setData(GENERAL_MULTIPLIER_EFFECTS, list);
                }
            }
        });
    }

    public static BlockPos findDispersedSpawnPos(ServerLevel level, double minDistance, double maxDistance) {
        List<Vec3> otherPositions = new ArrayList<>();

        for (ServerPlayer other : level.getServer().getPlayerList().getPlayers()) {
            otherPositions.add(other.position());
        }

        for (ServerPlayer known : level.getServer().getPlayerList().getPlayers()) {
            GlobalPos origin = known.getData(CraftorioDataAttachments.SPAWN_ORIGIN.get());
            otherPositions.add(new Vec3(origin.pos().getX(), 0, origin.pos().getZ()));
        }

        Random random = new Random();
        int maxAttempts = 200;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = minDistance + random.nextDouble() * (maxDistance - minDistance);

            int x = (int) Math.round(Math.cos(angle) * radius);
            int z = (int) Math.round(Math.sin(angle) * radius);

            boolean farEnough = true;
            for (Vec3 otherPos : otherPositions) {
                double dx = x - otherPos.x;
                double dz = z - otherPos.z;
                double distSq = dx * dx + dz * dz;
                if (distSq < minDistance * minDistance) {
                    farEnough = false;
                    break;
                }
            }

            if (!farEnough) continue;

            ChunkPos chunkPos = new ChunkPos(new BlockPos(x, 0, z));
            BlockPos candidate = PlayerRespawnLogic.getSpawnPosInChunk(level, chunkPos);
            if (candidate != null) {
                return candidate;
            }
        }

        double fallbackAngle = random.nextDouble() * Math.PI * 2;
        int fallbackX = (int) Math.round(Math.cos(fallbackAngle) * maxDistance);
        int fallbackZ = (int) Math.round(Math.sin(fallbackAngle) * maxDistance);

        BlockPos fallback = PlayerRespawnLogic.getSpawnPosInChunk(level, new ChunkPos(new BlockPos(fallbackX, 0, fallbackZ)));
        if (fallback != null) {
            return fallback;
        }

        return level.getSharedSpawnPos();
    }

    public static BlockPos getPlayerOrigin(Player player){
        return player.getData(SPAWN_ORIGIN).pos();
    }

    //Contract Checks
    public static List<CraftorioShipmentContract> getCraftorioContracts(Player player){
        Level level = player.level();
        if (universalBased(level)){
            return level.getData(SHIPMENT_CONTRACTS);
        } else {
            return player.getData(SHIPMENT_CONTRACTS);
        }
    }

    public static void setCraftorioContracts(Player player,List<CraftorioShipmentContract> contracts){
        Level level = player.level();
        if (universalBased(level)){
            level.setData(SHIPMENT_CONTRACTS,contracts);
        } else {
            player.setData(SHIPMENT_CONTRACTS,contracts);
        }
    }

    public static CraftorioShipmentContract makeCraftorioContract(List<CraftorioShipmentItem> contractItems,String name,int time){
        return new CraftorioShipmentContract(contractItems,name,time);
    }





}
