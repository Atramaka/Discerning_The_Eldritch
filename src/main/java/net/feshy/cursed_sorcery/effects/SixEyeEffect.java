package net.feshy.cursed_sorcery.effects;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;


public class SixEyeEffect extends MobEffect implements ISyncedMobEffect {
    public static final ResourceLocation EYE_TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "textures/entity/eyes/six_eyes.png");
    
    public SixEyeEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }
    
    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(modid = CursedSorcery.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class SixEyesRenderHandler {
        @SubscribeEvent
        public static void onPlayerRender(RenderPlayerEvent.Post event) {
            if (event.getEntity().hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT)) {
                // The eye texture will be rendered here
                // This is a placeholder - the actual rendering is handled by the GlowingEyesLayer
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 2 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {



        if (livingEntity.level().isClientSide && livingEntity == Minecraft.getInstance().player) {
            for (int i = 0; i < 3; i++) {
                Vec3 pos = new Vec3(Utils.getRandomScaled(16), Utils.getRandomScaled(5f) + 5, Utils.getRandomScaled(16)).add(livingEntity.position());
                Vec3 random = new Vec3(Utils.getRandomScaled(.08f), Utils.getRandomScaled(.08f), Utils.getRandomScaled(.08f));
                livingEntity.level().addParticle(ParticleTypes.WHITE_ASH, pos.x, pos.y, pos.z, random.x, random.y, random.z);
            }
        }
        return true;
    }


}