package net.feshy.cursed_sorcery.entity.armor.Geckolib;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.Geckolib.AscendedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class AscendedArmorModel extends DefaultedItemGeoModel<AscendedArmorItem> {

    public AscendedArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, ""));
    }

    @Override
    public ResourceLocation getModelResource(AscendedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/ascended_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AscendedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/models/armor/geckolib/ascended_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AscendedArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}
