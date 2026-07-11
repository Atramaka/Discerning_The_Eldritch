package net.feshy.cursed_sorcery.entity.render.armor;

import mod.azure.azurelib.common.render.armor.AzArmorRenderer;
import mod.azure.azurelib.common.render.armor.AzArmorRendererConfig;
import mod.azure.azurelib.common.render.layer.AzAutoGlowingLayer;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.items.armor.animators.EldritchArmorAnimator;
import net.minecraft.resources.ResourceLocation;

public class EldritchWarlockHelmetRenderer extends AzArmorRenderer {
    private static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "geo/eldritch_armor_helmet.geo.json"
    );

    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "textures/models/armor/eldritch_armor_helmet.png"
    );

    public EldritchWarlockHelmetRenderer() {
        super(
                AzArmorRendererConfig.builder(GEO, TEX)
                        .setAnimatorProvider(EldritchArmorAnimator::new)
                        .addRenderLayer(new AzAutoGlowingLayer<>())
                        .build()
        );
    }
}
