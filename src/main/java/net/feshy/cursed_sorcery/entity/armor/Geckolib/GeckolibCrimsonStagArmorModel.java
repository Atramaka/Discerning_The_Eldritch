package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.Geckolib.GeckolibCrimsonStagArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class GeckolibCrimsonStagArmorModel extends DefaultedItemGeoModel<GeckolibCrimsonStagArmorItem> {
    public GeckolibCrimsonStagArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, ""));
    }

    @Override
    public ResourceLocation getModelResource(GeckolibCrimsonStagArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/crimson_stag_geckolib.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GeckolibCrimsonStagArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/models/armor/geckolib/crimson_stag_gecko.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GeckolibCrimsonStagArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}
