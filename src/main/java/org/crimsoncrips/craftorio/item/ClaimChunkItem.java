package org.crimsoncrips.craftorio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.crimsoncrips.craftorio.CraftorioMisc;

import java.util.List;

public class ClaimChunkItem extends Item {

    public ClaimChunkItem(Properties properties) {
        super(properties);
    }



    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!CraftorioMisc.chunkBased(level)) return InteractionResult.PASS;

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ChunkPos chunkPos = new ChunkPos(context.getClickedPos());
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

        if (CraftorioMisc.isOwnedBy(chunk, player)) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.already_own_chunk").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        if (!CraftorioMisc.isNoBorders(level) && CraftorioMisc.isOwnedByAnother(chunk, player)) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.cannot_own_other_players_chunk").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        CraftorioMisc.setOwnedBy(chunk, player, true);
        CraftorioMisc.setLandAmount(CraftorioMisc.getLandAmount(player) + 1, player);

        if (!player.isCreative()) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("misc.craftorio.claim_item_tooltip")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
    }
}
