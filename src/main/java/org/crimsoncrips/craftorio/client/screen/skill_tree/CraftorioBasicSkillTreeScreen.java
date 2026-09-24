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
import org.crimsoncrips.craftorio.networking.skill_tree.UnlockUpgradePacket;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgrade;

import java.math.BigInteger;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class CraftorioBasicSkillTreeScreen extends CraftorioSkillTreeScreenBase {

    public CraftorioBasicSkillTreeScreen(String keyName) {
        super(Component.translatable(keyName));
    }

    @Override
    protected ResourceKey<Registry<CraftorioUpgrade>> registryKey() {
        return CraftorioUpgrade.REGISTRY_KEY;
    }

    @Override
    protected boolean hasUnlocked(Player player, ResourceLocation id) {
        return CraftorioMisc.hasUnlockedUpgrade(player, id);
    }

    @Override
    protected int getPurchaseCount(Player player, ResourceLocation id) {
        return CraftorioMisc.getUpgradeCount(player, id);
    }

    @Override
    protected Set<ResourceLocation> getUnlockedSnapshot(Player player) {
        return CraftorioMisc.getUnlockedUpgrades(player);
    }

    @Override
    protected BigInteger getCurrentCurrency(Player player) {
        return CraftorioMisc.getPoints(player);
    }

    @Override
    protected String formatCost(BigInteger cost) {
        return CraftorioMisc.bigIntFormat(cost);
    }

    @Override
    protected void sendUnlockPacket(ResourceLocation id) {
        PacketDistributor.sendToServer(new UnlockUpgradePacket(id));
    }

    @Override
    protected boolean useSpawnAnimation() {
        return true;
    }

    @Override
    protected void renderHud(GuiGraphics graphics, Player player) {
        String pointsLine = Component.translatable("misc.craftorio.points_label").getString()
                + CraftorioMisc.bigIntFormat(CraftorioMisc.getPoints(player));
        graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 8, 0xFFFF55);
    }
}
