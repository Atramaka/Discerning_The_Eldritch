package net.feshy.cursed_sorcery.events;

import io.redspace.ironsspellbooks.fluids.SimpleClientFluidType;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.render.ClientStaffItemExtensions;
import net.feshy.cursed_sorcery.CursedSorcery;

//import net.feshy.cursed_sorcery.entity.render.mobs.*;

import net.feshy.cursed_sorcery.entity.spells.dismantle.DismantleRenderer;
import net.feshy.cursed_sorcery.entity.spells.lapse_blue.LapseBlueRenderer;
import net.feshy.cursed_sorcery.entity.spells.reversal_red.ReversalRedRenderer;
import net.feshy.cursed_sorcery.entity.spells.slice.SliceRenderer;
import net.feshy.cursed_sorcery.particle.*;
import net.feshy.cursed_sorcery.registries.DTEEntityRegistry;
import net.feshy.cursed_sorcery.registries.DTEFluidRegistry;
import net.feshy.cursed_sorcery.registries.DTEParticleRegistry;
import net.feshy.cursed_sorcery.registries.ItemRegistries;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.feshy.cursed_sorcery.render.GlowingEyesLayer;



@EventBusSubscriber(modid = CursedSorcery.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DTEEntityRegistry.SLICE.get(), SliceRenderer::new);
        event.registerEntityRenderer(DTEEntityRegistry.DISMANTLE.get(), DismantleRenderer::new);

        event.registerEntityRenderer(DTEEntityRegistry.LAPSE_BLUE.get(), LapseBlueRenderer::new);
        event.registerEntityRenderer(DTEEntityRegistry.REVERSAL_RED.get(), ReversalRedRenderer::new);


//        event.registerEntityRenderer(DTEEntityRegistry.APOTHIC_SUMMONER.get(), ApothicSummonerRenderer::new);
//        event.registerEntityRenderer(DTEEntityRegistry.APOTHIC_CRUSADER.get(), ApothicCrusaderRenderer::new);
//        event.registerEntityRenderer(DTEEntityRegistry.APOTHIC_ACOLYTE.get(), ApothicAcolyteRenderer::new);
//        event.registerEntityRenderer(DTEEntityRegistry.ASCENDED_ONE.get(), AscendedOneRenderer::new);
//        event.registerEntityRenderer(DTEEntityRegistry.ASCENDED_ONE_CULTIST.get(), AscendedOneRenderer::new);
//        event.registerEntityRenderer(DTEEntityRegistry.GAOLER_ENTITY.get(), context -> {return new GaolerRenderer(context, new GaolerModel());});
//        event.registerEntityRenderer(DTEEntityRegistry.APOSTLE_ENTITY.get(), context -> {return new TheApostleRenderer(context, new TheApostleModel());});
//        event.registerEntityRenderer(DTEEntityRegistry.SIGHTLESS_MAW.get(), context -> {return new SightlessMawRenderer(context, new SightlessMawModel());});
//        event.registerEntityRenderer(DTEEntityRegistry.UNTOLD_BEHEMOTH.get(), context -> {return new UntoldBehemothRenderer(context, new UntoldBehemothModel());});

    }



    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        // Add glowing eyes layer to player renderers
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer playerRenderer = event.getSkin(skin);
            if (playerRenderer != null) {
                playerRenderer.addLayer(new GlowingEyesLayer.Vanilla<>(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event)
    {
        // Items
        event.registerItem(new ClientStaffItemExtensions(), ItemRegistries.getDTEItems().stream().filter(item -> item.get() instanceof StaffItem staffItem && !staffItem.hasCustomRendering()).map(holder -> (Item) holder.get()).toArray(Item[]::new));

        // Fluids
        event.registerFluidType(new SimpleClientFluidType(CursedSorcery.id("block/liquid_malice")), DTEFluidRegistry.LIQUID_MALICE_TYPE);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event)
    {





        event.registerSpriteSet(DTEParticleRegistry.GLACIAL_SHADOW_PARTICLE.get(), GlacialShadowParticle.Provider::new);
        event.registerSpriteSet(DTEParticleRegistry.ESOTERIC_SPARKS_PARTICLE.get(), EsotericSparksParticle.Provider::new);
        event.registerSpriteSet(DTEParticleRegistry.RIFT_SLICE_PARTICLE.get(), RiftSliceParticle.Provider::new);
        event.registerSpriteSet(DTEParticleRegistry.MALIGNANT_SOUL.get(), MalignantSoulParticle.Provider::new);
        event.registerSpriteSet(DTEParticleRegistry.MALIGNANT_FLAME.get(), MalignantFlameParticle.Provider::new);
        event.registerSpriteSet(DTEParticleRegistry.SOUL_FIRE_SLASH_PARTICLE.get(), SoulFireSlashParticle.Provider::new);

    }
}
