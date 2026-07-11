package net.feshy.cursed_sorcery.entity.spells.lapse_blue;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LapseBlueModel extends GeoModel<LapseBlueEntity> {
    @Override
    public ResourceLocation getModelResource(LapseBlueEntity lapseBlueEntity) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/betterblue.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LapseBlueEntity lapseBlueEntity) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/entity/lapse_blue/lapse_blue2.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LapseBlueEntity lapseBlueEntity) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animations/bluetest.animation.json");
    }
}
