package net.feshy.cursed_sorcery.items.armor.Geckolib;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.acetheeldritchking.aces_spell_utils.entity.render.armor.EmissiveGenericCustomArmorRenderer;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.entity.armor.Geckolib.GeckolibEldritchWarlockMaskModel;
import net.feshy.cursed_sorcery.items.armor.DTEArmorMaterialRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GeckolibEldritchWarlockMaskItem extends ImbuableGeckolibDTEArmorItem {
    public GeckolibEldritchWarlockMaskItem(Type slot, Properties settings) {
        super(DTEArmorMaterialRegistry.ELDRITCH_WARLOCK, slot, settings, schoolAttributesWithResistance(AttributeRegistry.ELDRITCH_SPELL_POWER, AttributeRegistry.SUMMON_DAMAGE, 150, 0.10F, 0.05F, 0.05F));
    }

    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath(
            CursedSorcery.MOD_ID,
            "textures/models/armor/geckolib/eldritch_warlock_mask_glowmask.png");

    private static final RenderType GLOW_RENDER_TYPE = RenderType.breezeEyes(LAYER);

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new EmissiveGenericCustomArmorRenderer<>(new GeckolibEldritchWarlockMaskModel(), LAYER, GLOW_RENDER_TYPE);
    }
}
