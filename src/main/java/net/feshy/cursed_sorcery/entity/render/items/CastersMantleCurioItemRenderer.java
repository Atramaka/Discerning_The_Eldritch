package net.feshy.cursed_sorcery.entity.render.items;

import mod.azure.azurelib.common.render.armor.AzArmorRenderer;
import mod.azure.azurelib.common.render.armor.AzArmorRendererConfig;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceLocation;

public class CastersMantleCurioItemRenderer extends AzArmorRenderer {
    public static final ResourceLocation GEO = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "geo/casters_mantle.geo.json"
    );

    public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "textures/models/armor/casters_mantle_armor.png"
    );

    public CastersMantleCurioItemRenderer() {
        super(
                AzArmorRendererConfig.builder(GEO, TEX)
                        .build()
        );
    }
}
