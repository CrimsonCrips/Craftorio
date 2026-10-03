package org.crimsoncrips.craftorio.client.screen.skill_tree;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;


@OnlyIn(Dist.CLIENT)
public class CraftorioBasicSkillTreeScreen extends CraftorioSkillTreeScreenBase {

    public CraftorioBasicSkillTreeScreen(String keyName) {
        super(Component.translatable(keyName));
    }

    @Override
    protected UpgradeTree tree() {
        return UpgradeTree.BASIC;
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
