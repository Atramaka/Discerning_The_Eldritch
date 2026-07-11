package net.feshy.cursed_sorcery.entity.spells.void_spike;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidSpikeModel extends GeoModel<VoidSpikeEntity> {
    @Override
    public ResourceLocation getModelResource(VoidSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/void_spike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/entity/void_tentacle/void_tentacle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animations/heavy_desolation.animation.json");
    }
}
