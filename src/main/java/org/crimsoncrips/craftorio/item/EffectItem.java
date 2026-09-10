package org.crimsoncrips.craftorio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.CraftorioDataComponents;
import org.crimsoncrips.craftorio.registries.effect.*;

import java.util.ArrayList;
import java.util.List;

public class EffectItem extends Item {

    public EffectItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        for (CraftorioEffects chosenEffect : getContainedEffects(itemstack)){
            if (chosenEffect instanceof TagMultiplierEffect tagMultiplierEffect) {
                List<TagMultiplierEffect> effects = new ArrayList<>(CraftorioMisc.getTagEffects(player));
                effects.add(tagMultiplierEffect);
                CraftorioMisc.setTagEffects(player,effects);
            }
            if (chosenEffect instanceof GeneralMultiplierEffect generalMultiplierEffect) {
                List<GeneralMultiplierEffect> effects = new ArrayList<>(CraftorioMisc.getGeneralEffects(player));
                effects.add(generalMultiplierEffect);
                CraftorioMisc.setGeneralEffects(player,effects);
            }
            if (chosenEffect instanceof ShopMultiplierEffect shopMultiplierEffect) {
                List<ShopMultiplierEffect> effects = new ArrayList<>(CraftorioMisc.getShopEffects(player));
                effects.add(shopMultiplierEffect);
                CraftorioMisc.setShopEffects(player,effects);
            }
        }
        if (!player.isCreative()) {
            player.getItemInHand(usedHand).shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        for (CraftorioEffects chosenEffect : getContainedEffects(stack)) {
            ChatFormatting color = ChatFormatting.BLUE;
            if ((chosenEffect instanceof TagMultiplierEffect tagEffect && tagEffect.getMultiplier() < 0)
                    || (chosenEffect instanceof GeneralMultiplierEffect generalEffect && generalEffect.getMultiplier() < 0)) {
                color = ChatFormatting.RED;
            }

            Component component = Component.literal(chosenEffect.getActualName() + " (" + CraftorioMisc.ticksToTimeString(chosenEffect.getTime()) + ")")
                    .withStyle(Style.EMPTY.withItalic(true).withColor(color));

            tooltipComponents.add(component);
        }
    }

    public List<CraftorioEffects> getContainedEffects(ItemStack itemStack) {
        return itemStack.get(CraftorioDataComponents.EFFECTS_STORED);
    }

    public void setContainedEffects(ItemStack itemStack,List<CraftorioEffects> containedEffects) {
        itemStack.set(CraftorioDataComponents.EFFECTS_STORED,containedEffects);
    }
}
