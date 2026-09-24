package org.crimsoncrips.craftorio.skill_tree.target;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

public enum AttributeTarget {
    HEALTH(Attributes.MAX_HEALTH),
    SPEED(Attributes.MOVEMENT_SPEED),
    DEFENSE(Attributes.ARMOR),
    DAMAGE(Attributes.ATTACK_DAMAGE),
    BLOCK_REACH(Attributes.BLOCK_INTERACTION_RANGE),
    JUMP_HEIGHT(Attributes.JUMP_STRENGTH),
    XP_GAIN(null),
    RESISTANCE(Attributes.ARMOR_TOUGHNESS);

    private final Holder<Attribute> attribute;

    AttributeTarget(Holder<Attribute> attribute) {
        this.attribute = attribute;
    }

    public Holder<Attribute> getAttribute() {
        return attribute;
    }
}
