package org.crimsoncrips.craftorio.client.screen.skill_tree;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;


@OnlyIn(Dist.CLIENT)
public class CraftorioSacrificeSkillTreeScreen extends CraftorioSkillTreeScreenBase {

    public CraftorioSacrificeSkillTreeScreen() {
        super(Component.translatable("misc.craftorio.sacrifice_skill_tree_title"));
    }

    @Override
    protected UpgradeTree tree() {
        return UpgradeTree.SACRIFICE;
    }

    @Override
    protected void renderHud(GuiGraphics graphics, Player player) {
        String pointsLine = Component.translatable("misc.craftorio.sacrifice_points_current", CraftorioMisc.getSacrificePoints(player).toString()).getString();
        graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 12, 0xFF5555);
    }
}
