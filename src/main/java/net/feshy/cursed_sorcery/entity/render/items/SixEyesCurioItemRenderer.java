package net.feshy.cursed_sorcery.entity.render.items;

import mod.azure.azurelib.common.render.armor.AzArmorRenderer;
import mod.azure.azurelib.common.render.armor.AzArmorRendererConfig;
import mod.azure.azurelib.common.render.layer.AzAutoGlowingLayer;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceLocation;

public class SixEyesCurioItemRenderer extends AzArmorRenderer {
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "geo/six_eyes.geo.json"
    );

    public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "textures/models/armor/six_eyes.png"
    );

    public SixEyesCurioItemRenderer() {
        super(
                AzArmorRendererConfig.builder(GEO, TEX)
//                        .setAnimatorProvider(KingsVisageAnimator::new)
                        .addRenderLayer(new AzAutoGlowingLayer<>())
                        .build()
        );
    }
}
