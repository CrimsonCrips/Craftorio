package org.crimsoncrips.craftorio.client.screen.skill_tree;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;


@OnlyIn(Dist.CLIENT)
public class CraftorioRebirthSkillTreeScreen extends CraftorioSkillTreeScreenBase {

    public CraftorioRebirthSkillTreeScreen() {
        super(Component.translatable("misc.craftorio.rebirth_skill_tree_title"));
    }

    @Override
    protected UpgradeTree tree() {
        return UpgradeTree.REBIRTH;
    }

    @Override
    protected void renderHud(GuiGraphics graphics, Player player) {
        String lifeLine = Component.translatable("misc.craftorio.rebirth_current_life", CraftorioMisc.getLife(player)).getString();
        String pointsLine = Component.translatable("misc.craftorio.rebirth_life_points_current", CraftorioMisc.getLifePoints(player).toString()).getString();
        graphics.drawCenteredString(this.font, lifeLine, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, pointsLine, this.width / 2, 20, 0xFFFF55);
    }
}
