package org.crimsoncrips.craftorio.server.skill_tree;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;

import java.math.BigInteger;

public final class SkillTreeReveal {

    public static final int HIDDEN = 0;
    public static final int PENDING = 1;
    public static final int REVEALED = 2;

    private SkillTreeReveal() {}

    public static int state(Player player, UpgradeTree tree) {
        AttachmentType<Integer> type = playerType(tree);
        return type == null ? REVEALED : player.getData(type);
    }

    public static void unlock(ServerPlayer player, UpgradeTree tree) {
        AttachmentType<Integer> type = playerType(tree);
        if (type == null) return;

        if (CraftorioMisc.universalBased(player.level()) && player.getServer() != null) {
            player.getServer().overworld().setData(worldType(tree), true);
            for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
                markUnlocked(online, type);
            }
        } else {
            markUnlocked(player, type);
        }
    }

    public static void markRevealed(ServerPlayer player, UpgradeTree tree) {
        AttachmentType<Integer> type = playerType(tree);
        if (type != null && player.getData(type) == PENDING) {
            player.setData(type, REVEALED);
        }
    }

    public static void onLogin(ServerPlayer player) {
        if (player.getServer() == null) return;
        ServerLevel overworld = player.getServer().overworld();

        if (player.getData(CraftorioDataAttachments.REBIRTH_TREE_REVEAL.get()) == HIDDEN) {
            if (CraftorioMisc.getLife(player) > 1 || !CraftorioMisc.getUpgradePurchaseCounts(player, UpgradeTree.REBIRTH).isEmpty()
                    || CraftorioMisc.getLifePoints(player).compareTo(BigInteger.ZERO) > 0) {
                player.setData(CraftorioDataAttachments.REBIRTH_TREE_REVEAL.get(), REVEALED);
            } else if (CraftorioMisc.universalBased(overworld) && overworld.getData(CraftorioDataAttachments.WORLD_REBIRTH_TREE_UNLOCKED)) {
                player.setData(CraftorioDataAttachments.REBIRTH_TREE_REVEAL.get(), PENDING);
            }
        }

        if (player.getData(CraftorioDataAttachments.SACRIFICE_TREE_REVEAL.get()) == HIDDEN) {
            if (CraftorioMisc.getSacrificePoints(player).compareTo(BigInteger.ZERO) > 0 || !CraftorioMisc.getUpgradePurchaseCounts(player, UpgradeTree.SACRIFICE).isEmpty()) {
                player.setData(CraftorioDataAttachments.SACRIFICE_TREE_REVEAL.get(), REVEALED);
            } else if (CraftorioMisc.universalBased(overworld) && overworld.getData(CraftorioDataAttachments.WORLD_SACRIFICE_TREE_UNLOCKED)) {
                player.setData(CraftorioDataAttachments.SACRIFICE_TREE_REVEAL.get(), PENDING);
            }
        }
    }

    private static void markUnlocked(ServerPlayer player, AttachmentType<Integer> type) {
        if (player.getData(type) == HIDDEN) {
            player.setData(type, PENDING);
        }
    }

    private static AttachmentType<Integer> playerType(UpgradeTree tree) {
        return switch (tree) {
            case BASIC -> null;
            case REBIRTH -> CraftorioDataAttachments.REBIRTH_TREE_REVEAL.get();
            case SACRIFICE -> CraftorioDataAttachments.SACRIFICE_TREE_REVEAL.get();
        };
    }

    private static AttachmentType<Boolean> worldType(UpgradeTree tree) {
        return tree == UpgradeTree.REBIRTH ? CraftorioDataAttachments.WORLD_REBIRTH_TREE_UNLOCKED.get() : CraftorioDataAttachments.WORLD_SACRIFICE_TREE_UNLOCKED.get();
    }
}
