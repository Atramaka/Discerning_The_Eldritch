package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibEldritchWarlockArmorItem;
import net.minecraft.resources.ResourceLocation;

public interface GeckolibEldritchWarlockInterface {
    ResourceLocation getModelResource(GeckolibEldritchWarlockArmorItem object);

    ResourceLocation getTextureResource(GeckolibEldritchWarlockArmorItem object);

    ResourceLocation getAnimationResource(GeckolibEldritchWarlockArmorItem animatable);
}
