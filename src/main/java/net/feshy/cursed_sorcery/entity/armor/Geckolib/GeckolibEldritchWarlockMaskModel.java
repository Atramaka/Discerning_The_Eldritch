package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibEldritchWarlockMaskItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class GeckolibEldritchWarlockMaskModel extends DefaultedItemGeoModel<GeckolibEldritchWarlockMaskItem> implements GeckolibEldritchMaskInterface {

    public GeckolibEldritchWarlockMaskModel() {
        super(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, ""));
    }

    @Override
    public ResourceLocation getModelResource(GeckolibEldritchWarlockMaskItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/eldritch_armor_mask_geckolib.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeckolibEldritchWarlockMaskItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/models/armor/geckolib/eldritch_warlock_mask.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeckolibEldritchWarlockMaskItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animations/eldritch_armor_geckolib.animation.json");
    }
}
