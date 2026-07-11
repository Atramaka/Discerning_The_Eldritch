package net.feshy.cursed_sorcery.mixins;

import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Check if entity has the float effect
        if (entity.hasEffect(DTEPotionEffectRegistry.FLOAT_EFFECT) ) {
            // Reduce downward velocity dramatically
            double currentVelocityY = entity.getDeltaMovement().y;

            // Only apply if falling (negative velocity)
            if (currentVelocityY < 0) {
                // Reduce fall speed to 2% of normal (nearly suspended)
                // Adjust this value:
                // 0.01 = 1% fall speed (extremely slow)
                // 0.02 = 2% fall speed (almost suspended)
                // 0.05 = 5% fall speed (very slow)
                double newVelocityY = currentVelocityY * 0.1;

                entity.setDeltaMovement(
                        entity.getDeltaMovement().x,
                        newVelocityY,
                        entity.getDeltaMovement().z
                );
            }
        }
    }
}