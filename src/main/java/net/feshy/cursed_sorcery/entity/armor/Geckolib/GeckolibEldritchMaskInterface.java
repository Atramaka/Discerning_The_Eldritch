package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibEldritchWarlockMaskItem;
import net.minecraft.resources.ResourceLocation;

public interface GeckolibEldritchMaskInterface {
    ResourceLocation getModelResource(GeckolibEldritchWarlockMaskItem object);

    ResourceLocation getTextureResource(GeckolibEldritchWarlockMaskItem object);

    ResourceLocation getAnimationResource(GeckolibEldritchWarlockMaskItem animatable);
}
