package org.crimsoncrips.craftorio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

import java.util.List;

public class MysteryEffectRune extends EffectRune {

    public MysteryEffectRune(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (getContainedEffects(stack) == null)
            return;
        for (CraftorioEffects chosenEffect : getContainedEffects(stack)) {
            ChatFormatting color = ChatFormatting.WHITE;

            Component component = Component.literal(chosenEffect.getActualName())
                    .withStyle(Style.EMPTY.withObfuscated(true).withColor(color));

            tooltipComponents.add(component);
        }
    }
}
