package net.feshy.cursed_sorcery.utils;

import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceLocation;

public class FSpellAnimations {
    public static ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animation");

    public static final AnimationHolder ANIMATION_POINT = new AnimationHolder(CursedSorcery.id("point"), true);
    public static final AnimationHolder ANIMATION_BLUE_SUMMON = new AnimationHolder(CursedSorcery.id("blue_summon"), true);
}
