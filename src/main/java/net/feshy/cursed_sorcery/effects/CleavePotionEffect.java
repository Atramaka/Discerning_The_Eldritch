package net.feshy.cursed_sorcery.effects;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.TraceParticleOptions;
import net.feshy.cursed_sorcery.registries.DTEDamageTypes;

import net.feshy.cursed_sorcery.registries.DTEParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CleavePotionEffect extends MobEffect {
    public static DamageSource DAMAGE_SOURCE;

    /** How often to damage (1 = 20x/sec, 2 = 10x/sec, 3 = 6.6x/sec...) */
    private static final int DAMAGE_INTERVAL_TICKS = 2;

    public CleavePotionEffect() {
        super(MobEffectCategory.HARMFUL, 11749198);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!(livingEntity.level() instanceof ServerLevel world)) {
            return true;
        }

        if (DAMAGE_SOURCE == null) {
            DAMAGE_SOURCE = new DamageSource(DamageSources.getHolderFromResource(livingEntity, DTEDamageTypes.RAZOR_DAMAGE));
        }

        // Spawn continuous slash particles around the target every tick
        spawnCleaveLashes(world, livingEntity, amplifier);

        // Apply rapid damage
        float damagePerTick = 1f + (amplifier * 0.5f);

        // Cancel knockback from the damage tick
        Vec3 oldMotion = livingEntity.getDeltaMovement();
        DamageSources.ignoreNextKnockback(livingEntity);
        DamageSources.applyDamage(livingEntity, damagePerTick, DAMAGE_SOURCE);
        livingEntity.setDeltaMovement(oldMotion);

        // Keep rapid hits registering
        livingEntity.invulnerableTime = 0;

        return true;
    }


    private void spawnCleaveLashes(ServerLevel world, LivingEntity target, int amplifier) {
        // Only spawn particles if the target is alive
        if (!target.isAlive()) {
            return;
        }

        // Only spawn particles every 3 ticks
        if (world.getGameTime() % 3 != 0) {
            return;
        }

        int lashCount = 2 + amplifier;

        for (int i = 0; i < lashCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double height = Math.random() * (target.getBbHeight() - 0.2) + 0.1;
            double radius = target.getBbWidth() * 0.6;

            Vec3 center = target.getBoundingBox().getCenter().add(
                    Math.cos(angle) * radius,
                    height - target.getBbHeight() * 0.5,
                    Math.sin(angle) * radius
            );

            // Create multiple slash trails using trace particles
            int slashCount = 3 + (int)(Math.random() * 2); // 3-4 slashes per lash

            for (int s = 0; s < slashCount; s++) {
                // Randomize slash direction in 3D space (not just horizontal)
                // Use spherical coordinates for full 3D randomization
                double theta = Math.random() * Math.PI * 2;  // Horizontal rotation (0 to 360 degrees)
                double phi = Math.random() * Math.PI;         // Vertical rotation (0 to 180 degrees)

                double slashLength = 1.2 + Math.random() * 0.8;

                // Convert spherical coordinates to direction vector
                double dirX = Math.sin(phi) * Math.cos(theta);
                double dirY = Math.cos(phi);
                double dirZ = Math.sin(phi) * Math.sin(theta);

                Vec3 slashDirection = new Vec3(dirX, dirY, dirZ).normalize().scale(slashLength);

                Vec3 slashStart = center.add(
                        (Math.random() - 0.5) * 0.3,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.3
                );

                Vec3 slashEnd = slashStart.add(slashDirection);

                // Random color variation (reddish shades for cleave effect)
                float red = 1f;
                float green = 1f ;
                float blue = 1f ;

                // Spawn trace particle
                MagicManager.spawnParticles(world,
                        new TraceParticleOptions(Utils.v3f(slashEnd), new Vector3f(red, green, blue)),
                        slashStart.x, slashStart.y, slashStart.z,
                        1, 0, 0, 0, 0, false);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL_TICKS == 0;
    }
}