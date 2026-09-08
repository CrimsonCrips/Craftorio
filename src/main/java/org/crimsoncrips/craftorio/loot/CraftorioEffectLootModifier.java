package org.crimsoncrips.craftorio.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.crimsoncrips.craftorio.CraftorioDataComponents;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.util.ArrayList;
import java.util.List;

public class CraftorioEffectLootModifier extends LootModifier {

    public static final MapCodec<CraftorioEffectLootModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, CraftorioEffectLootModifier::new));

    private static final float SPAWN_CHANCE = 0.03f;

    public CraftorioEffectLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CraftorioLootModifiers.EFFECT_ITEM_LOOT.get();
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation tableId = context.getQueriedLootTableId();
        if (tableId == null || !tableId.getNamespace().equals("minecraft") || !tableId.getPath().startsWith("chests/")) {
            return generatedLoot;
        }

        RandomSource random = context.getRandom();
        if (random.nextFloat() >= SPAWN_CHANCE) {
            return generatedLoot;
        }

        int effectCount = 1 + random.nextInt(2);
        List<CraftorioEffects> chosen = new ArrayList<>();
        for (int i = 0; i < effectCount; i++) {
            chosen.add(CraftorioMisc.getRandomEffect(context.getLevel().registryAccess(), random));
        }

        ItemStack stack = new ItemStack(random.nextBoolean() ? CraftorioItems.EFFECT_ITEM.get() : CraftorioItems.MYSTERY_EFFECT_ITEM);
        stack.set(CraftorioDataComponents.EFFECTS_STORED.get(), chosen);
        generatedLoot.add(stack);

        return generatedLoot;
    }
}
