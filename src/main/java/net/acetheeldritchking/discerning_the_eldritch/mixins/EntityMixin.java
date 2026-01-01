package net.acetheeldritchking.discerning_the_eldritch.mixins;

import net.acetheeldritchking.discerning_the_eldritch.registries.DTEPotionEffectRegistry;
import net.acetheeldritchking.discerning_the_eldritch.utils.IEntityDataAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntityMixin extends Entity implements IEntityDataAccessor {

    @Shadow public abstract boolean hasEffect(Holder<MobEffect> pEffect);

    public EntityMixin(net.minecraft.world.entity.EntityType<?> p_19870_, net.minecraft.world.level.Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public void dte$setFallFlying(boolean flying) {
        this.setSharedFlag(7, flying);
    }

    /**
     * This stops the upward-facing flickering.
     * We cancel the vanilla Elytra check when the effect is active, forcing the flight flag to stay true.
     */
    @Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
    private void dte$bypassElytraCheck(CallbackInfo ci) {
        if (this.hasEffect(DTEPotionEffectRegistry.DESTRUCTIVE_RAMPAGE_EFFECT) || this.hasEffect(DTEPotionEffectRegistry.VOID_SURF_EFFECT)) {
            if (!this.isPassenger()) {
                this.dte$setFallFlying(true);
                ci.cancel(); 
            }
        }
    }
}
