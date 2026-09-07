package org.crimsoncrips.craftorio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;
import org.crimsoncrips.craftorio.registries.effect.CraftorioPointEffect;

import java.util.List;

public class MysteryEffectItem extends EffectItem {

    public MysteryEffectItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        for (CraftorioEffects chosenEffect : getContainedEffects(stack)) {
            ChatFormatting color = ChatFormatting.WHITE;

            Component component = Component.literal(chosenEffect.getActualName())
                    .withStyle(Style.EMPTY.withObfuscated(true).withColor(color));

            tooltipComponents.add(component);
        }
    }
}
