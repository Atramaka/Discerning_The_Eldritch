package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibEldritchWarlockHelmetItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class GeckolibEldritchWarlockHelmetModel extends DefaultedItemGeoModel<GeckolibEldritchWarlockHelmetItem> implements GeckolibEldritchHelmetInterface {

    public GeckolibEldritchWarlockHelmetModel() {
        super(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, ""));
    }

    @Override
    public ResourceLocation getModelResource(GeckolibEldritchWarlockHelmetItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/eldritch_armor_helmet_geckolib.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeckolibEldritchWarlockHelmetItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/models/armor/geckolib/eldritch_armor_helmet.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeckolibEldritchWarlockHelmetItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animations/eldritch_armor_geckolib.animation.json");
    }
}
