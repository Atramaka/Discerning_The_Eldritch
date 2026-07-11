package net.feshy.cursed_sorcery.effects;

import io.redspace.ironsspellbooks.effect.CustomDescriptionMobEffect;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.feshy.cursed_sorcery.registries.SpellRegistries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;
import net.feshy.cursed_sorcery.utils.IEntityDataAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VoidSurfPotionEffect extends CustomDescriptionMobEffect {

    public VoidSurfPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x191919);
    }

    @Override
    public Component getDescriptionLine(net.minecraft.world.effect.MobEffectInstance instance) {
        return Component.translatable("tooltip.cursed_sorcery.void_surf_description");
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
    DustParticleOptions indicatorColor = new DustParticleOptions(new Vector3f(0.3f, 0.0f, 0.7f), 0.5f);
    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (!(living instanceof Player player)) {
            return true;
        }

        MobEffectInstance effectInstance = player.getEffect(DTEPotionEffectRegistry.VOID_SURF_EFFECT);
        int currentDuration = effectInstance != null ? effectInstance.getDuration() : 0;
        int maxDuration = 60 + ((amplifier + 1) * 10); // Sync with getDuration()

        // 1. 0.3s DELAY (6 ticks): Skip movement logic during initial leap
        if (currentDuration > (maxDuration - 6)) {
            return true;
        }

        // 2. ANIMATION & STABILITY
        if (!player.onGround()) {
            player.setPose(Pose.FALL_FLYING);
            ((IEntityDataAccessor) player).dte$setFallFlying(true);
            player.setSwimming(false);
            player.setSprinting(false);
        }

        // 3. Particle trail
        if (player.level().isClientSide) {
            for (int i = 0; i < 10; i++) {
                player.level().addParticle(
                        ParticleTypes.ASH,
                        player.getX() + (Math.random() - 0.5) * 0.6,
                        player.getY() + (Math.random() * 0.8),
                        player.getZ() + (Math.random() - 0.5) * 0.6,
                        (Math.random() - 0.5) * 0.05,
                        (Math.random() - 0.5) * 0.05,
                        (Math.random() - 0.5) * 0.05
                );



            }

            for (int i = 0; i < 3; i++) {
                player.level().addParticle(
                        indicatorColor,
                        player.getX() + (Math.random() - 0.5) * 0.6,
                        player.getY() + (Math.random() * 0.8),
                        player.getZ() + (Math.random() - 0.5) * 0.6,
                        (Math.random() - 0.5) * 0.05,
                        (Math.random() - 0.5) * 0.05,
                        (Math.random() - 0.5) * 0.05
                );
            }
        }

        // 4. IMPACT & DAMAGE LOGIC
        boolean hitWall = !player.level().noCollision(player.getBoundingBox().move(player.getDeltaMovement()).move(player.getDeltaMovement().normalize().scale(0.1)).deflate(0.1));
        boolean hitGround = player.onGround() && player.getDeltaMovement().y <= 0;

        if (hitWall || hitGround) {
            if (!player.level().isClientSide && player.level() instanceof ServerLevel sl) {
                float radius = 4.0f;
                float damage = 6.0f + (amplifier * 2.0f);

                // Apply AOE Damage
                List<Entity> targets = sl.getEntities(player, player.getBoundingBox().inflate(radius));
                for (Entity target : targets) {
                    if (target instanceof LivingEntity livingTarget && target != player) {
                        if (target.distanceToSqr(player) < radius * radius) {
                            DamageSources.applyDamage(livingTarget, damage, SpellRegistries.VOID_SURF.get().getDamageSource(player));
                        }
                    }
                }

                // Shockwave VFX


                MagicManager.spawnParticles(sl, new BlastwaveParticleOptions(new Vector3f(0.05f, 0.05f, 0.05f), radius), player.getX(), player.getY() + 0.1, player.getZ(), 1, 0, 0, 0, 0, true);
                sl.sendParticles(ParticleTypes.WARPED_SPORE, player.getX(), player.getY(), player.getZ(), 50, 1.5, 0.5, 1.5, 0.45);

                sl.sendParticles(indicatorColor, player.getX(), player.getY(), player.getZ(), 20, 1.5, 0.5, 1.5, 0.15);

                sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY(), player.getZ(), 10, 0.8, 0.2, 0.8, 0.05);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), DTESoundRegistry.SOUL_SLAM.value(), player.getSoundSource(), 4, 0.8f);
            }
            player.addTag("void_surf_impact");
            player.removeEffect(DTEPotionEffectRegistry.VOID_SURF_EFFECT);
            return false;
        }

        // 5. FLIGHT ENGINE
        double speed = 1.55D + (amplifier * 0.2D);
        double lerpFactor = 0.25D;

        Vec3 look = player.getLookAngle().normalize();
        Vec3 currentVel = player.getDeltaMovement();
        Vec3 targetVel = look.scale(speed);

        double gravityCounteract = 0.085D;

        Vec3 newVel = new Vec3(
                Mth.lerp(lerpFactor, currentVel.x, targetVel.x),
                Mth.lerp(lerpFactor, currentVel.y, targetVel.y) + gravityCounteract,
                Mth.lerp(lerpFactor, currentVel.z, targetVel.z)
        );

        player.setDeltaMovement(newVel);
        player.fallDistance = 0;
        player.hasImpulse = true;

        return true;
    }

    @Override
    public void onEffectRemoved(LivingEntity living, int amplifier) {
        if (living instanceof Player player) {
            // Check if we hit a wall/ground (tag exists) or timed out (tag missing)
            if (!player.removeTag("void_surf_impact")) {
                // TELEPORT DOWN: Only triggers on natural timeout
                Level level = player.level();
                Vec3 start = player.position();
                Vec3 end = start.add(0, -64, 0);
                BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                if (hit.getType() == HitResult.Type.BLOCK) {
                    Vec3 groundPos = hit.getLocation().add(0, 0.1, 0);
                    player.teleportTo(groundPos.x, groundPos.y, groundPos.z);
                    player.resetFallDistance();

                    if (!level.isClientSide && level instanceof ServerLevel sl) {
                        sl.sendParticles(indicatorColor, player.getX(), player.getY(), player.getZ(), 20, 1.5, 0.5, 1.5, 0.15);
                        sl.sendParticles(ParticleTypes.SQUID_INK, groundPos.x, groundPos.y, groundPos.z, 45, 0.6, 0.2, 0.6, 0.1);
                        sl.sendParticles(ParticleTypes.ASH, groundPos.x, groundPos.y, groundPos.z, 25, 0.5, 0.2, 0.5, 0.05);
                    }
                }
            }

            ((IEntityDataAccessor) player).dte$setFallFlying(false);
            player.setPose(Pose.STANDING);
        }
        super.onEffectRemoved(living, amplifier);
    }
}
