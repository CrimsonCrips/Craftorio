package org.crimsoncrips.craftorio.client.screen.skill_tree;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.skill_tree.UnlockRebirthUpgradePacket;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class CraftorioRebirthSkillTreeScreen extends CraftorioSkillTreeScreenBase {

    public CraftorioRebirthSkillTreeScreen() {
        super(Component.translatable("misc.craftorio.rebirth_skill_tree_title"));
    }

    @Override
    protected ResourceKey<Registry<CraftorioUpgrade>> registryKey() {
        return CraftorioUpgrade.REBIRTH_REGISTRY_KEY;
    }

    @Override
    protected boolean hasUnlocked(Player player, ResourceLocation id) {
        return CraftorioMisc.hasUnlockedRebirthUpgrade(player, id);
    }

    @Override
    protected int getPurchaseCount(Player player, ResourceLocation id) {
        return CraftorioMisc.getRebirthUpgradeCount(player, id);
    }

    @Override
    protected Set<ResourceLocation> getUnlockedSnapshot(Player player) {
        return CraftorioMisc.getRebirthUpgradePurchaseCounts(player).keySet();
    }

    @Override
    protected BigInteger getCurrentCurrency(Player player) {
        return CraftorioMisc.getLifePoints(player);
    }

    @Override
    protected String formatCost(BigInteger cost) {
        return cost.toString();
    }

    @Override
    protected void sendUnlockPacket(ResourceLocation id) {
        PacketDistributor.sendToServer(new UnlockRebirthUpgradePacket(id));
    }

    @Override
    protected void renderHud(GuiGraphics graphics, Player player) {
        String lifeLine = Component.translatable("misc.craftorio.rebirth_current_life", CraftorioMisc.getLife(player)).getString();
        String pointsLine = Component.translatable("misc.craftorio.rebirth_life_points_current", CraftorioMisc.getLifePoints(player).toString()).getString();
        graphics.drawCenteredString(this.font, lifeLine, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 20, 0xFFFF55);
    }
}
