package net.feshy.cursed_sorcery.entity.spells.void_spike;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidSpikeRenderer extends GeoEntityRenderer<VoidSpikeEntity> {
    public VoidSpikeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VoidSpikeModel());
    }

}
