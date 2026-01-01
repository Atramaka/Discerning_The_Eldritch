package net.acetheeldritchking.discerning_the_eldritch.spells.dark;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTESchoolRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class OblivionSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "oblivion");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(DTESchoolRegistry.VOID_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(30)
            .build();

    public OblivionSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 20;
        this.baseManaCost = 80;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.WARDEN_SONIC_BOOM);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.SCULK_CATALYST_BLOOM);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (level.getGameTime() % 4 == 0) { // Render every 4 ticks to save performance
            float radius = getRadius(spellLevel, entity);
            Vec3 centerPos = entity.getBoundingBox().getCenter();
            spawnAOEIndicator(level, centerPos, radius, false);
        }
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        float radius = getRadius(spellLevel, entity);
        float damage = getDamage(spellLevel, entity);
        Vec3 centerPos = entity.getBoundingBox().getCenter();

        // Spawn full AOE indicator and caster's "Void Ball"
        spawnAOEIndicator(level, centerPos, radius, true);
        spawnVoidVortexBall(level, centerPos, 1f);

        // Get all entities in radius
        List<Entity> targets = level.getEntities(entity, entity.getBoundingBox().inflate(radius, radius, radius),
                (target) -> !DamageSources.isFriendlyFireBetween(target, entity));

        // Process each hit target
        for (Entity target : targets) {
            if (target instanceof LivingEntity livingEntity && canHit(entity, target)
                    && livingEntity.distanceToSqr(entity) < radius * radius) {

                Vec3 oldPos = livingEntity.getBoundingBox().getCenter();

                // Apply damage
                DamageSources.applyDamage(target, damage, getDamageSource(entity));

                // Teleport enemy to random location within AOE
                teleportToRandomLocation(livingEntity, centerPos, radius);

                // Capture new position after teleport
                Vec3 newPos = livingEntity.getBoundingBox().getCenter();

                // Spawn connection beam between old and new positions
                spawnConnectionBeam(level, oldPos, newPos);

                // Engulf enemy at the new location
                spawnVoidVortexBall(level, newPos, livingEntity.getBbWidth() + 0.5f);
                spawnVoidBlast(level, newPos);
            }
        }

        // Camera shake for dramatic effect
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, entity.position(), radius));

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnVoidVortexBall(Level level, Vec3 pos, float ballRadius) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Create a dense shell of particles
        for (int i = 0; i < 60; i++) {
            Vec3 randomOffset = Utils.getRandomVec3(ballRadius);
            Vec3 particlePos = pos.add(randomOffset);

            serverLevel.sendParticles(ParticleTypes.ASH, particlePos.x, particlePos.y, particlePos.z, 5, 0, 0, 0, 0.05);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, particlePos.x, particlePos.y, particlePos.z, 10, 0, 0, 0, 0.0f);

            if (i % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.WARPED_SPORE, particlePos.x, particlePos.y, particlePos.z, 5, 0, 0, 0, 2.0f);
            }
        }
    }


    /**
     * Spawns an AOE field indicator on the ground
     */
    private void spawnAOEIndicator(Level level, Vec3 center, float radius, boolean isFullBlast) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Use BlastwaveParticleOptions for a clean, visible ground ring
        Vector3f voidColor = new Vector3f(0.1f, 0.0f, 0.15f);

        // Only spawn the solid ring and heavy effects if it's the final blast
        if (isFullBlast) {
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(voidColor, radius), center.x, center.y - 0.5f, center.z, 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(new Vector3f(0, 0, 0), radius * 0.95f), center.x, center.y - 0.45f, center.z, 1, 0, 0, 0, 0, true);
        }

        // DENSE PARTICLE RING: Increased multiplier from 4 to 16 for a solid line
        int particleCount = isFullBlast ? (int) (radius * 16) : (int) (radius * 6);
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / (particleCount);
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            // Increased per-point count and slightly adjusted spread to ensure overlap
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, x, center.y - 0.5, z, 4, 0.2, 0.2, 0.2, 0.00f);

            if (isFullBlast || serverLevel.random.nextFloat() < 0.3f) {
                serverLevel.sendParticles(ParticleTypes.WARPED_SPORE, x, center.y - 0.5, z, 12, 0.1, 0.1, 0.1, 2f);
            }
        }
    }

    /**
     * Spawns a connection beam from caster to target
     */
    private void spawnConnectionBeam(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Create multiple particles along the beam for a glowing connection effect
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        for (double i = 0; i < distance; i += 0.3) {
            Vec3 beamPos = start.add(direction.scale(i));
            
            // Mix of soul particles and ink for void effect
            serverLevel.sendParticles(ParticleTypes.SMOKE, beamPos.x, beamPos.y, beamPos.z, 15, 0.05, 0.05, 0.05, 0f);
            serverLevel.sendParticles(ParticleTypes.WARPED_SPORE, beamPos.x, beamPos.y, beamPos.z, 5, 0.02, 0.02, 0.02, 2.0f);
        }
    }

    /**
     * Spawns void blast effect at target location
     */
    private void spawnVoidBlast(Level level, Vec3 targetPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Burst of malignant soul particles
        MagicManager.spawnParticles(level, ParticleTypes.WARPED_SPORE, targetPos.x, targetPos.y, targetPos.z, 1, 0.4, 0.4, 0.4, 0.05f, false);
        
        // Ash and ink particles for void effect
        serverLevel.sendParticles(ParticleTypes.ASH, targetPos.x, targetPos.y, targetPos.z, 5, 0.3, 0.3, 0.3, 0.15);
        serverLevel.sendParticles(ParticleTypes.SQUID_INK, targetPos.x, targetPos.y, targetPos.z, 4, 0.2, 0.2, 0.2, 0.005f);
    }

    /**
     * Teleports the target to a random location within the AOE radius
     */
    private void teleportToRandomLocation(LivingEntity target, Vec3 aoiCenter, float radius) {
        // Generate random location within AOE
        double angle = Math.random() * 2 * Math.PI;
        double randomRadius = Math.random() * radius;
        
        double newX = aoiCenter.x + randomRadius * Math.cos(angle);
        double newY = aoiCenter.y;
        double newZ = aoiCenter.z + randomRadius * Math.sin(angle);
        
        // Teleport the entity
        target.teleportTo(newX, newY, newZ);
        target.invulnerableTime = 0;
    }

    private boolean canHit(Entity owner, Entity target) {
        return target != owner && target.isAlive() && target.isPickable() && !target.isSpectator();
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return 6 + (spellLevel * 1.2f);
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return 5 + (getSpellPower(spellLevel, caster) * 0.4f);
    }

    @Override
    public void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent((soundEvent -> entity.playSound(soundEvent, 2.5f, 0.8f + Utils.random.nextFloat() * 0.4f)));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }
}
