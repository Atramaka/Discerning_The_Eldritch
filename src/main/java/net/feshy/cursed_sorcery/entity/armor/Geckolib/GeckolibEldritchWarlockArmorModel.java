package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibEldritchWarlockArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class GeckolibEldritchWarlockArmorModel extends DefaultedItemGeoModel<GeckolibEldritchWarlockArmorItem> implements GeckolibEldritchWarlockInterface {

    public GeckolibEldritchWarlockArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, ""));
    }

    @Override
    public ResourceLocation getModelResource(GeckolibEldritchWarlockArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/eldritch_armor_geckolib.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeckolibEldritchWarlockArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/models/armor/geckolib/eldritch_mage_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeckolibEldritchWarlockArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animations/eldritch_armor_geckolib.animation.json");
    }
}
