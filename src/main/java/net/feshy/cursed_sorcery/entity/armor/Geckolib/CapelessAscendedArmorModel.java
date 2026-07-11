package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.Geckolib.CapelessAscendedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CapelessAscendedArmorModel extends DefaultedItemGeoModel<CapelessAscendedArmorItem> {

    public CapelessAscendedArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, ""));
    }

    @Override
    public ResourceLocation getModelResource(CapelessAscendedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/ascended_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CapelessAscendedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/models/armor/geckolib/ascended_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CapelessAscendedArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}
