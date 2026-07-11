package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibEldritchWarlockHelmetItem;
import net.minecraft.resources.ResourceLocation;

public interface GeckolibEldritchHelmetInterface {
    ResourceLocation getModelResource(GeckolibEldritchWarlockHelmetItem object);

    ResourceLocation getTextureResource(GeckolibEldritchWarlockHelmetItem object);

    ResourceLocation getAnimationResource(GeckolibEldritchWarlockHelmetItem animatable);
}
