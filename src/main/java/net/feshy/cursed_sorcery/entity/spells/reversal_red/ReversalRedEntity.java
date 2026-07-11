package net.feshy.cursed_sorcery.entity.spells.reversal_red;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.feshy.cursed_sorcery.registries.DTEEntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

/**
 * Purely visual entity for Reversal Red - all logic is handled in the spell
 */
public class ReversalRedEntity extends AoeEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> DATA_MODEL_SCALE = SynchedEntityData.defineId(ReversalRedEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_FOLLOW_OWNER_LOOK = SynchedEntityData.defineId(ReversalRedEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_FOLLOW_DISTANCE = SynchedEntityData.defineId(ReversalRedEntity.class, EntityDataSerializers.FLOAT);

    // Longer lifetime to cover the full cast time (40 ticks cast + buffer)
    private static final int LIFETIME = 100;
    private int ticksAlive = 0;

    public ReversalRedEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public ReversalRedEntity(Level level, LivingEntity owner) {
        this(DTEEntityRegistry.REVERSAL_RED.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODEL_SCALE, 1.0f);
        builder.define(DATA_FOLLOW_OWNER_LOOK, false);
        builder.define(DATA_FOLLOW_DISTANCE, 1.5f);
    }

    public void setModelScale(float scale) {
        this.entityData.set(DATA_MODEL_SCALE, scale);
    }

    public float getModelScale() {
        return this.entityData.get(DATA_MODEL_SCALE);
    }

    public void setFollowOwnerLook(boolean follow) {
        this.entityData.set(DATA_FOLLOW_OWNER_LOOK, follow);
    }

    public boolean shouldFollowOwnerLook() {
        return this.entityData.get(DATA_FOLLOW_OWNER_LOOK);
    }

    public void setFollowDistance(float distance) {
        this.entityData.set(DATA_FOLLOW_DISTANCE, distance);
    }

    public float getFollowDistance() {
        return this.entityData.get(DATA_FOLLOW_DISTANCE);
    }

    @Override
    public void tick() {
        super.tick();

        ticksAlive++;
        if (ticksAlive >= LIFETIME) {
            discard();
            return;
        }

        // Follow owner's look direction if enabled
        if (shouldFollowOwnerLook() && getOwner() instanceof LivingEntity owner && owner.isAlive()) {
            Vec3 eyePos = owner.getEyePosition();
            Vec3 lookVec = owner.getLookAngle();
            float followDist = getFollowDistance();
            Vec3 targetPos = eyePos.add(lookVec.scale(followDist));

            // Smooth movement towards target position
            Vec3 currentPos = this.position();
            Vec3 direction = targetPos.subtract(currentPos);
            double distance = direction.length();

            // Lerp towards target for smooth movement
            if (distance > 0.05) {
                double lerpFactor = Math.min(0.3, distance); // Smooth interpolation
                Vec3 newPos = currentPos.add(direction.normalize().scale(lerpFactor));
                this.setPos(newPos.x, newPos.y, newPos.z);
            } else {
                this.setPos(targetPos.x, targetPos.y, targetPos.z);
            }

            // Match owner's rotation
            this.setYRot(owner.getYRot());
            this.setXRot(owner.getXRot());
        }

        // Spawn visual particles on both client and server
        spawnVisualParticles();
    }

    private void spawnVisualParticles() {
        Vec3 center = this.position();

        if (level().isClientSide()) {
            // Crimson spores swirling
            for (int i = 0; i < 3; i++) {
                double angle = Math.random() * Math.PI * 2;
                double dist = 0.3 + Math.random() * 0.5;
                double x = center.x + Math.cos(angle) * dist;
                double y = center.y + (Math.random() - 0.5) * 1.0;
                double z = center.z + Math.sin(angle) * dist;

                level().addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z,
                        (Math.random() - 0.5) * 0.05, 0.02, (Math.random() - 0.5) * 0.05);
            }

            // Red-colored dust particles
            if (tickCount % 3 == 0) {
                float red = 1.0f;
                float green = 0.2f + (float)(Math.random() * 0.15f);
                float blue = 0.1f;
                float size = 1.2f + (float)(Math.random() * 0.5f);

                DustParticleOptions redDust = new DustParticleOptions(
                        new Vector3f(red, green, blue), size);

                level().addParticle(redDust,
                        center.x + (Math.random() - 0.5) * 0.3,
                        center.y + (Math.random() - 0.5) * 0.3,
                        center.z + (Math.random() - 0.5) * 0.3,
                        0, -0.02, 0);

                // Darker red ember particles
                DustParticleOptions emberDust = new DustParticleOptions(
                        new Vector3f(0.8f, 0.1f, 0.05f), 0.8f);
                level().addParticle(emberDust,
                        center.x + (Math.random() - 0.5) * 0.5,
                        center.y + (Math.random() - 0.5) * 0.5,
                        center.z + (Math.random() - 0.5) * 0.5,
                        (Math.random() - 0.5) * 0.03, 0.02, (Math.random() - 0.5) * 0.03);
            }
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
        // No effect - purely visual
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(1.0, 1.0, 1.0);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    private final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.red_idle.new");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            state.getController().setAnimation(IDLE_ANIM);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
