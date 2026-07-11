package net.feshy.cursed_sorcery.entity.spells.reversal_red;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ReversalRedModel extends GeoModel<ReversalRedEntity> {
    @Override
    public ResourceLocation getModelResource(ReversalRedEntity ReversalRedEntity) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "geo/red.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ReversalRedEntity ReversalRedEntity) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/entity/reversal_red/reversal_red.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ReversalRedEntity ReversalRedEntity) {
        return ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "animations/reversal_red.animation.json");
    }
}
