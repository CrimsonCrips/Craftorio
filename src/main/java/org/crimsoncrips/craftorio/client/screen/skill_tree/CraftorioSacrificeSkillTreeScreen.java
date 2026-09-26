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
import org.crimsoncrips.craftorio.networking.skill_tree.UnlockSacrificeUpgradePacket;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class CraftorioSacrificeSkillTreeScreen extends CraftorioSkillTreeScreenBase {

    public CraftorioSacrificeSkillTreeScreen() {
        super(Component.translatable("misc.craftorio.sacrifice_skill_tree_title"));
    }

    @Override
    protected ResourceKey<Registry<CraftorioUpgrade>> registryKey() {
        return CraftorioUpgrade.SACRIFICE_REGISTRY_KEY;
    }

    @Override
    protected boolean hasUnlocked(Player player, ResourceLocation id) {
        return CraftorioMisc.hasUnlockedSacrificeUpgrade(player, id);
    }

    @Override
    protected int getPurchaseCount(Player player, ResourceLocation id) {
        return CraftorioMisc.getSacrificeUpgradeCount(player, id);
    }

    @Override
    protected Set<ResourceLocation> getUnlockedSnapshot(Player player) {
        return CraftorioMisc.getSacrificeUpgradePurchaseCounts(player).keySet();
    }

    @Override
    protected BigInteger getCurrentCurrency(Player player) {
        return CraftorioMisc.getSacrificePoints(player);
    }

    @Override
    protected String formatCost(BigInteger cost) {
        return cost.toString();
    }

    @Override
    protected void sendUnlockPacket(ResourceLocation id) {
        PacketDistributor.sendToServer(new UnlockSacrificeUpgradePacket(id));
    }

    @Override
    protected void renderHud(GuiGraphics graphics, Player player) {
        String pointsLine = Component.translatable("misc.craftorio.sacrifice_points_current", CraftorioMisc.getSacrificePoints(player).toString()).getString();
        graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 12, 0xFF5555);
    }
}
