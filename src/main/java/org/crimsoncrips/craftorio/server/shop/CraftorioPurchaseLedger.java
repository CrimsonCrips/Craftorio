package org.crimsoncrips.craftorio.server.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public final class CraftorioPurchaseLedger {

    public record Purchase(long count, BigInteger spent) {
        public static final Codec<Purchase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("count").forGetter(Purchase::count),
                CraftorioMisc.BIGINT_CODEC().fieldOf("spent").forGetter(Purchase::spent)
        ).apply(instance, Purchase::new));

        BigInteger paidPerUnit() {
            return count <= 0 ? BigInteger.ZERO : spent.divide(BigInteger.valueOf(count));
        }
    }

    public record Sunk(int bought, BigInteger value) {}

    private CraftorioPurchaseLedger() {}

    private static IAttachmentHolder holder(Player player) {
        Level level = CraftorioMisc.universalLevel(player);
        return CraftorioMisc.universalBased(level) ? level : player;
    }

    public static Map<ResourceLocation, Purchase> get(Player player) {
        return holder(player).getData(CraftorioDataAttachments.PURCHASED_ITEMS);
    }

    public static void record(Player player, Item item, int quantity, BigInteger totalPaid) {
        if (quantity <= 0) return;
        Map<ResourceLocation, Purchase> updated = new HashMap<>(get(player));
        updated.merge(BuiltInRegistries.ITEM.getKey(item), new Purchase(quantity, totalPaid),
                (existing, added) -> new Purchase(existing.count() + added.count(), existing.spent().add(added.spent())));
        holder(player).setData(CraftorioDataAttachments.PURCHASED_ITEMS, updated);
    }

    public static void clear(Player player) {
        holder(player).setData(CraftorioDataAttachments.PURCHASED_ITEMS, new HashMap<>());
    }

    public static Session open(Player player) {
        return new Session(player);
    }

    public static final class Session {
        private final Player player;
        private final Map<ResourceLocation, Purchase> remaining;
        private boolean changed;

        private Session(Player player) {
            this.player = player;
            this.remaining = new HashMap<>(get(player));
        }

        public Sunk sink(ItemStack stack, boolean manualSink) {
            BigInteger full = CraftorioMisc.checkValue(stack, player, true, manualSink);
            int count = stack.getCount();
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Purchase purchase = remaining.get(key);
            if (purchase == null || purchase.count() <= 0 || count <= 0) return new Sunk(0, full);

            int bought = (int) Math.min(count, purchase.count());
            BigInteger paidPerUnit = purchase.paidPerUnit();
            BigInteger perUnit = full.divide(BigInteger.valueOf(count));
            BigInteger boughtValue = perUnit.min(paidPerUnit).multiply(BigInteger.valueOf(bought));
            BigInteger freeValue = full.multiply(BigInteger.valueOf(count - bought)).divide(BigInteger.valueOf(count));

            take(key, purchase, bought, paidPerUnit);
            return new Sunk(bought, boughtValue.add(freeValue));
        }

        public int reserve(ItemStack stack) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Purchase purchase = remaining.get(key);
            if (purchase == null || purchase.count() <= 0) return 0;

            int bought = (int) Math.min(stack.getCount(), purchase.count());
            take(key, purchase, bought, purchase.paidPerUnit());
            return bought;
        }

        private void take(ResourceLocation key, Purchase purchase, int amount, BigInteger paidPerUnit) {
            long left = purchase.count() - amount;
            if (left <= 0) {
                remaining.remove(key);
            } else {
                remaining.put(key, new Purchase(left, purchase.spent().subtract(paidPerUnit.multiply(BigInteger.valueOf(amount))).max(BigInteger.ZERO)));
            }
            changed = true;
        }

        public void commit() {
            if (changed) {
                holder(player).setData(CraftorioDataAttachments.PURCHASED_ITEMS, new HashMap<>(remaining));
            }
        }
    }
}
