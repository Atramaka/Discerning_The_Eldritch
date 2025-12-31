package net.acetheeldritchking.discerning_the_eldritch.entity.spells.void_spike;

import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidSpikeModel extends GeoModel<VoidSpikeEntity> {
    @Override
    public ResourceLocation getModelResource(VoidSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "geo/void_spike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "textures/entity/void_tentacle/void_tentacle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "animations/heavy_desolation.animation.json");
    }
}
