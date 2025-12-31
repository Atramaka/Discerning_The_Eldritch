package net.acetheeldritchking.discerning_the_eldritch.entity.spells.void_spike;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.acetheeldritchking.discerning_the_eldritch.particle.DTEParticleHelper;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTEEntityRegistry;
import net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class VoidSpikeEntity extends AoeEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VoidSpikeEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public VoidSpikeEntity(Level level, LivingEntity owner, float damage) {
        this(DTEEntityRegistry.VOID_SPIKE.get(), level);
        setOwner(owner);
        setDamage(damage);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        if (target != getOwner() && !target.isAlliedTo(getOwner())) {
            DamageSources.applyDamage(target, getDamage(), SpellRegistries.HEAVY_DESOLATION.get().getDamageSource(this, getOwner()));
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.2, 0));
        }
    }

    @Override
    public void tick() {
        super.tick(); // Call super to ensure base AOE logic works
        
        if (tickCount == hitTick) {
            if (!level().isClientSide) {
                checkHits();
            } else {
                spawnVoidParticles();
            }
        }

        if (!level().isClientSide && tickCount > deathTick) {
            discard();
        }
    }

    private void spawnVoidParticles() {
        for (int i = 0; i < 15; i++) {
            Vec3 motion = new Vec3(
                    (random.nextDouble() - 0.5) * 0.2,
                    random.nextDouble() * 0.5,
                    (random.nextDouble() - 0.5) * 0.2
            );
            
            // Soul Particle
            level().addParticle(DTEParticleHelper.MALIGNANT_SOUL, getX(), getY() + 0.5, getZ(), motion.x, motion.y, motion.z);
            // Ash Particle
            level().addParticle(ParticleTypes.ASH, getX(), getY() + 0.2, getZ(), motion.x, motion.y, motion.z);
            // Ink Particle
            level().addParticle(ParticleTypes.SQUID_INK, getX(), getY() + 0.8, getZ(), motion.x * 0.5, motion.y * 0.5, motion.z * 0.5);
        }
    }

    // Geckolib Logic
    private final RawAnimation RISE_ANIM = RawAnimation.begin().thenPlay("rise");
    private final RawAnimation FALL_ANIM = RawAnimation.begin().thenPlay("fall");
    private final int hitTick = 8;
    private final int fallTick = 18;
    private final int deathTick = 25;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "spike_controller", 0, event -> {
            if (tickCount < fallTick) {
                return event.setAndContinue(RISE_ANIM);
            } else {
                return event.setAndContinue(FALL_ANIM);
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public float getParticleCount() { return 0; }

    @Override
    public Optional<ParticleOptions> getParticle() { return Optional.empty(); }
}
