package net.feshy.cursed_sorcery.spells.curse;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.acetheeldritchking.aces_spell_utils.spells.ASSpellAnimations;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.entity.spells.reversal_red.ReversalRedEntity;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.feshy.cursed_sorcery.registries.DTESchoolRegistry;
import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.feshy.cursed_sorcery.registries.SpellRegistries;
import net.feshy.cursed_sorcery.utils.FSpellAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@AutoSpellConfig
public class RedExplodeSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "reversal_red");
    private static final Random random = new Random();

    // Cone settings
    private static final float BASE_CONE_HALF_ANGLE = 10f; // Base degrees from center
    private static final float BASE_CONE_LENGTH = 3f;
    private static final float EFFECT_START_DISTANCE = 1.5f; // Distance from caster before effects begin

    // Block destruction/fling chances (0.0 to 1.0)
    private static final float BASE_BLOCK_DESTROY_CHANCE = 0.95f;  // 40% base chance to destroy
    private static final float BASE_BLOCK_FLING_CHANCE = 0.05f;    // 10% base chance to fling

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.cursed_sorcery.length", Utils.stringTruncation(getConeLength(spellLevel, caster), 1)),
                Component.translatable("ui.cursed_sorcery.cone_angle", Utils.stringTruncation(getConeHalfAngle(spellLevel, caster) * 2, 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(DTESchoolRegistry.CURSE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(60)
            .build();

    public RedExplodeSpell() {
        this.manaCostPerLevel = 100;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 45;
        this.baseManaCost = 250;
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
        return Optional.of(SoundRegistry.TELEKINESIS_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(DTESoundRegistry.LAPSE_BLUE_SUMMON.get());
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
        if (playerMagicData == null)
            return;

        if (entity.hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT)) {
            // Apply slow falling effect to the caster
            entity.addEffect(new MobEffectInstance(
                    DTEPotionEffectRegistry.FLOAT_EFFECT,
                    this.castTime + 30, // Duration in ticks (10 seconds)
                    0,   // Amplifier (level)
                    false, // Ambient
                    true   // Show particles
            ));
        }

        // Calculate initial spawn position in front of the player
        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookVec = entity.getLookAngle();
        float followDistance = 1.5f;
        Vec3 spawnPos = eyePos.add(lookVec.scale(followDistance));

        // Spawn the red orb entity during charge-up
        ReversalRedEntity redEntity = new ReversalRedEntity(level, entity);
        redEntity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, entity.getYRot(), entity.getXRot());
        redEntity.setFollowOwnerLook(true);
        redEntity.setFollowDistance(followDistance);

        // Tag it so we can find and remove it later
        redEntity.addTag("reversal_red_charging_" + entity.getId());

        level.addFreshEntity(redEntity);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        Vec3 origin = entity.getEyePosition();
        Vec3 direction = entity.getLookAngle().normalize();
        float coneLength = getConeLength(spellLevel, entity);
        float coneHalfAngle = getConeHalfAngle(spellLevel, entity);
        float damage = getDamage(spellLevel, entity);
        float knockback = getKnockback(spellLevel, entity);

        // Find and despawn the charging red entity with explosion effects
        String redEntityTag = "reversal_red_charging_" + entity.getId();
        for (Entity e : serverLevel.getEntities(entity, entity.getBoundingBox().inflate(20))) {
            if (e instanceof ReversalRedEntity redEntity && redEntity.getTags().contains(redEntityTag)) {
                // Spawn explosion effects at the red entity's location
                spawnRedEntityExplosion(serverLevel, redEntity.position());
                redEntity.discard();
                break;
            }
        }

        // Play powerful sounds
        playExplosionSounds(serverLevel, origin);

        // Spawn initial flash VFX (RED themed)
        spawnInitialBlast(serverLevel, origin, direction);

        // Process blocks in cone - pass spellLevel for scaling
        processBlocksInCone(serverLevel, origin, direction, coneLength, coneHalfAngle, entity, spellLevel);

        // Push and damage entities in cone
        pushEntitiesInCone(serverLevel, origin, direction, coneLength, coneHalfAngle, damage, knockback, entity);

        // Spawn particle trail through cone
        spawnConeParticleTrail(serverLevel, origin, direction, coneLength, coneHalfAngle);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }



    private void spawnVanityEntity(Level level, LivingEntity caster, Vec3 origin, Vec3 direction) {
        ReversalRedEntity redEntity = new ReversalRedEntity(level, caster);
        Vec3 spawnPos = origin.add(direction.scale(2.0));
        redEntity.moveTo(spawnPos);
        redEntity.setModelScale(1.5f);
        level.addFreshEntity(redEntity);
    }

    private void spawnRedEntityExplosion(ServerLevel level, Vec3 position) {
        // Burst of particles from the entity
        for (int i = 0; i < 40; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = random.nextDouble() * Math.PI;
            double speed = 0.4 + random.nextDouble() * 0.6;

            double vx = speed * Math.sin(phi) * Math.cos(theta);
            double vy = speed * Math.cos(phi);
            double vz = speed * Math.sin(phi) * Math.sin(theta);

            level.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0f, 0.2f, 0.1f), 2.0f),
                    position.x, position.y, position.z,
                    1, vx, vy, vz, 0.15);
        }

        // Crimson spore burst
        level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                position.x, position.y, position.z,
                20, 0.5, 0.5, 0.5, 0.15);

        // Flash effect
        level.sendParticles(ParticleTypes.FLASH,
                position.x, position.y, position.z,
                3, 0.1, 0.1, 0.1, 0);

        // Sound effect
        level.playSound(null, position.x, position.y, position.z,
                DTESoundRegistry.LAPSE_BLUE_EXPLODE.get(), SoundSource.PLAYERS, 2.0f, 1.2f);
    }

    private void playExplosionSounds(ServerLevel level, Vec3 origin) {
        level.playSound(null, origin.x, origin.y, origin.z,
                DTESoundRegistry.LAPSE_BLUE_EXPLODE.get(), SoundSource.PLAYERS, 4.0f, 0.5f);
        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 0.7f);
    }

    private void spawnInitialBlast(ServerLevel level, Vec3 origin, Vec3 direction) {
        // Offset the blast start position away from the caster
        Vec3 blastOrigin = origin.add(direction.scale(EFFECT_START_DISTANCE+2));

        // Flash particles
        level.sendParticles(ParticleTypes.FLASH, blastOrigin.x, blastOrigin.y, blastOrigin.z, 5, 0.1, 0.1, 0.1, 0);

        // Dense red burst at origin
        for (int i = 0; i < 60; i++) {
            double speed = 0.5 + random.nextDouble() * 0.8;
            Vec3 vel = direction.scale(speed).add(
                    (random.nextDouble() - 0.5) * 0.4,
                    (random.nextDouble() - 0.5) * 0.4,
                    (random.nextDouble() - 0.5) * 0.4
            );

            level.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0f, 0.1f, 0.1f), 2.0f),
                    blastOrigin.x, blastOrigin.y, blastOrigin.z,
                    1, vel.x, vel.y, vel.z, 0.2);
        }

        // Red explosion particles instead of sonic boom
        for (int i = 0; i < 25; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double spread = random.nextDouble() * 2.0;
            level.sendParticles(
                    new DustColorTransitionOptions(
                            new Vector3f(1.0f, 0.3f, 0.2f),
                            new Vector3f(0.8f, 0.0f, 0.0f), 2.5f),
                    blastOrigin.x + Math.cos(angle) * spread,
                    blastOrigin.y + (random.nextDouble() - 0.5) * spread,
                    blastOrigin.z + Math.sin(angle) * spread,
                    2, direction.x * 0.3, direction.y * 0.3, direction.z * 0.3, 0.15);
        }
    }

    private void processBlocksInCone(ServerLevel level, Vec3 origin, Vec3 direction, float length, float coneHalfAngle, LivingEntity caster, int spellLevel) {

        // Check if terrain destruction is enabled
        if (!net.feshy.cursed_sorcery.utils.DTEServerConfig.enableTerrainDestruction) {
            return;
        }

        Set<BlockPos> blocksToDestroy = new HashSet<>();
        Set<BlockPos> blocksToFling = new HashSet<>();

        double coneAngleRad = Math.toRadians(coneHalfAngle);

        // Scale sampling based on spell level
        float levelMultiplier = 0.5f + (spellLevel * 5f);

        // Get configurable chances
        float destroyChance = getBlockDestroyChance(spellLevel, caster);
        float flingChance = getBlockFlingChance(spellLevel, caster);

        // Sample blocks in TRUE 3D cone (not flat) - start further from caster
        for (float dist = EFFECT_START_DISTANCE; dist <= length; dist += 0.4f / levelMultiplier) {
            // Cone radius expands with distance
            float coneRadius = (float) (dist * Math.tan(coneAngleRad));

            // Sample in spherical pattern at this distance
            int baseSamples = (int) (20 + dist * 4);
            int samples = (int) (baseSamples * levelMultiplier);

            for (int i = 0; i < samples; i++) {
                // Full 3D spherical sampling (not just horizontal)
                double theta = random.nextDouble() * Math.PI * 2;  // Full 360 horizontal
                double phi = random.nextDouble() * Math.PI;  // Full vertical coverage (0 to 180 degrees)
                double r = random.nextDouble() * coneRadius;

                // Calculate perpendicular vectors for proper 3D cone
                Vec3 right = new Vec3(-direction.z, 0, direction.x).normalize();
                Vec3 up = direction.cross(right).normalize();

                // True 3D cone position using spherical coordinates
                double radialDistance = r * Math.sin(phi);
                double verticalOffset = r * Math.cos(phi);

                Vec3 checkPos = origin
                        .add(direction.scale(dist))
                        .add(right.scale(Math.cos(theta) * radialDistance))
                        .add(up.scale(Math.sin(theta) * radialDistance))
                        .add(direction.scale(verticalOffset * 0.3));  // Slight forward spread

                BlockPos blockPos = BlockPos.containing(checkPos);

                if (blocksToDestroy.contains(blockPos) || blocksToFling.contains(blockPos)) continue;

                BlockState state = level.getBlockState(blockPos);
                if (state.isAir()) continue;

                float hardness = state.getDestroySpeed(level, blockPos);
                if (hardness < 0 || hardness >= 3.5f) continue;

                float distFromCenter = (float) r / coneRadius;

                // Use configurable fling chance - remove the distance modifier that was inflating the chance
                float effectiveFlingChance = flingChance;

                // First check if block should be affected at all based on destroy chance
                if (random.nextFloat() < destroyChance) {
                    // Then determine if it should be flung or just destroyed
                    if (random.nextFloat() < effectiveFlingChance) {
                        blocksToFling.add(blockPos);
                    } else {
                        blocksToDestroy.add(blockPos);
                    }
                }
            }
        }

        // Destroy blocks with particles
        for (BlockPos pos : blocksToDestroy) {
            level.destroyBlock(pos, false);
            if (random.nextFloat() < 0.8f) {
                spawnBlockDestructionParticles(level, pos.getCenter(), direction);
            }
        }

        // Fling blocks
        for (BlockPos pos : blocksToFling) {
            flingBlock(level, pos, origin, direction);
        }
    }


    private void flingBlock(ServerLevel level, BlockPos pos, Vec3 origin, Vec3 direction) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        level.destroyBlock(pos, false);

        // Create a falling block using the standard method
        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state);

        fallingBlock.setHurtsEntities(2.5f, 40);
        fallingBlock.time = 1;
        fallingBlock.dropItem = false;

        Vec3 blockCenter = pos.getCenter();
        Vec3 fromOrigin = blockCenter.subtract(origin).normalize();

        // MODERATE velocity that allows natural gravity curve
        // Instead of extreme speeds, use realistic push forces
        double blastPush = 0.6 + random.nextDouble() * 0.4;      // Forward blast (0.6-1.0)
        double outwardPush = 0.3 + random.nextDouble() * 0.2;    // Outward radial (0.3-0.5)
        double upwardPush = 0.4 + random.nextDouble() * 0.3;     // Upward (0.4-0.7)

        Vec3 velocity = direction.scale(blastPush)
                .add(fromOrigin.scale(outwardPush))
                .add(0, upwardPush, 0)
                .add(
                        (random.nextDouble() - 0.5) * 0.3,
                        0,
                        (random.nextDouble() - 0.5) * 0.3
                );

        // Set velocity before adding to world
        fallingBlock.setDeltaMovement(velocity);

        level.addFreshEntity(fallingBlock);

        // Dust particles pushed by blast
        spawnPushedParticles(level, pos.getCenter(), direction);
    }

    private void spawnPushedParticles(ServerLevel level, Vec3 center, Vec3 direction) {
        // Particles that are pushed by the blast
        for (int i = 0; i < 8; i++) {
            double vx = direction.x * (0.6 + random.nextDouble() * 0.5);
            double vy = 0.2 + random.nextDouble() * 0.6;
            double vz = direction.z * (0.6 + random.nextDouble() * 0.5);

            level.sendParticles(ParticleTypes.SMOKE,
                    center.x, center.y, center.z,
                    1, vx, vy, vz, 0.3);
        }
    }

    private void pushEntitiesInCone(ServerLevel level, Vec3 origin, Vec3 direction, float length, float coneHalfAngle, float damage, float knockback, LivingEntity caster) {
        double coneAngleRad = Math.toRadians(coneHalfAngle);

        AABB searchArea = new AABB(
                origin.subtract(length, length, length),
                origin.add(length, length, length)
        );

        List<Entity> entities = level.getEntities(caster, searchArea, e -> e instanceof LivingEntity && e != caster);

        for (Entity entity : entities) {
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(origin);
            double distanceToEntity = toEntity.length();

            if (distanceToEntity > length) continue;

            // Check if within cone angle
            double angle = Math.acos(toEntity.normalize().dot(direction));
            if (angle > coneAngleRad) continue;

            if (entity instanceof LivingEntity livingTarget) {
                // Distance falloff
                double distanceFactor = 1.0 - (distanceToEntity / length) * 0.4;
                float finalDamage = (float) (damage * distanceFactor);
                DamageSources.applyDamage(livingTarget, finalDamage,
                        SpellRegistries.REVERSAL_RED.get().getDamageSource(caster, caster));

                // Knockback in direction of blast
                Vec3 knockbackVec = direction.add(
                        (random.nextDouble() - 0.5) * 0.2,
                        0.2,
                        (random.nextDouble() - 0.5) * 0.2
                ).normalize().scale(knockback * distanceFactor);

                livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(knockbackVec.x, knockbackVec.y + 0.5, knockbackVec.z));
                livingTarget.hurtMarked = true;
            }
        }
    }

    private void spawnConeParticleTrail(ServerLevel level, Vec3 origin, Vec3 direction, float length, float coneHalfAngle) {
        double coneAngleRad = Math.toRadians(coneHalfAngle);
        Vec3 right = new Vec3(-direction.z, 0, direction.x).normalize();
        Vec3 up = direction.cross(right).normalize();

        // Start the particle trail further from the caster
        for (float dist = EFFECT_START_DISTANCE; dist < length; dist += 0.5f) {
            float coneRadius = (float) (dist * Math.tan(coneAngleRad));
            Vec3 center = origin.add(direction.scale(dist));

            // Heavy smoke pushed by blast - now 3D with velocity
            int smokeCount = (int) (10 + dist * 3);
            for (int i = 0; i < smokeCount; i++) {
                double theta = random.nextDouble() * Math.PI * 2;
                double phi = random.nextDouble() * Math.PI;
                double r = random.nextDouble() * coneRadius;

                // 3D distribution
                double radialDist = r * Math.sin(phi);
                double vertOffset = r * Math.cos(phi);

                Vec3 smokePos = center
                        .add(right.scale(Math.cos(theta) * radialDist))
                        .add(up.scale(Math.sin(theta) * radialDist))
                        .add(direction.scale(vertOffset * 0.2));

                // Velocity pushed by blast
                double smokeVx = direction.x * (0.15 + random.nextDouble() * 0.15);
                double smokeVy = 0.05 + random.nextDouble() * 0.1;
                double smokeVz = direction.z * (0.15 + random.nextDouble() * 0.15);

                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        smokePos.x, smokePos.y, smokePos.z,
                        1, smokeVx, smokeVy, smokeVz, 0.02);
            }

            // Red dust particles - 3D spread with velocity
            for (int i = 0; i < 20; i++) {
                double theta = random.nextDouble() * Math.PI * 2;
                double phi = random.nextDouble() * Math.PI;
                double r = random.nextDouble() * coneRadius;

                double radialDist = r * Math.sin(phi);
                double vertOffset = r * Math.cos(phi);

                Vec3 dustPos = center
                        .add(right.scale(Math.cos(theta) * radialDist))
                        .add(up.scale(Math.sin(theta) * radialDist))
                        .add(direction.scale(vertOffset * 0.3));

                double dustVx = direction.x * (0.2 + random.nextDouble() * 0.15);
                double dustVy = 0.05 + random.nextDouble() * 0.08;
                double dustVz = direction.z * (0.2 + random.nextDouble() * 0.15);

                level.sendParticles(
                        new DustParticleOptions(
                                new Vector3f(1.0f, 0.15f + random.nextFloat() * 0.15f, 0.1f), 0.4f),
                        dustPos.x, dustPos.y, dustPos.z,
                        1, dustVx, dustVy, dustVz, 0.1);
            }

            // Crimson spores - 3D
            level.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    center.x, center.y, center.z,
                    10, coneRadius * 0.8, coneRadius * 0.8, coneRadius * 0.8, 0.12);
        }

        // Impact at end of cone
        Vec3 impactPoint = origin.add(direction.scale(length));
        float finalRadius = (float) (length * Math.tan(coneAngleRad));

        level.sendParticles(ParticleTypes.EXPLOSION,
                impactPoint.x, impactPoint.y, impactPoint.z,
                6, finalRadius * 0.5, finalRadius * 0.5, finalRadius * 0.5, 0);

        // Dense red dust cloud at impact - 3D burst
        for (int i = 0; i < 40; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = random.nextDouble() * Math.PI;
            double r = random.nextDouble() * finalRadius;

            double radialDist = r * Math.sin(phi);
            double vertOffset = r * Math.cos(phi);

            Vec3 burstPos = impactPoint
                    .add(new Vec3(Math.cos(theta) * radialDist, vertOffset, Math.sin(theta) * radialDist));

            double burstVx = direction.x * (0.3 + random.nextDouble() * 0.2);
            double burstVy = 0.1 + random.nextDouble() * 0.15;
            double burstVz = direction.z * (0.3 + random.nextDouble() * 0.2);

            level.sendParticles(
                    new DustColorTransitionOptions(
                            new Vector3f(1.0f, 0.25f, 0.15f),
                            new Vector3f(0.7f, 0.05f, 0.05f), 1.6f),
                    burstPos.x, burstPos.y, burstPos.z,
                    1, burstVx, burstVy, burstVz, 0.12);
        }
    }

    private void spawnBlockDestructionParticles(ServerLevel level, Vec3 pos, Vec3 direction) {
        // Red dust
        level.sendParticles(
                new DustColorTransitionOptions(
                        new Vector3f(1.0f, 0.2f, 0.15f),
                        new Vector3f(0.5f, 0.1f, 0.1f), 1.2f),
                pos.x, pos.y, pos.z,
                8, 0.3, 0.3, 0.3, 0.06);

        // Heavy smoke
//        level.sendParticles(ParticleTypes.DUST_PLUME,
//                pos.x, pos.y, pos.z,
//                1, 0.25, 0.25, 0.25, 0.08);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity) * 3f;
    }

    private float getConeLength(int spellLevel, LivingEntity entity) {
        // Base length + scaling with spell power
        return BASE_CONE_LENGTH + (getSpellPower(spellLevel, entity) * 3f);
    }

    private float getConeHalfAngle(int spellLevel, LivingEntity entity) {
        // Base angle + scaling with spell level (caps at reasonable angle)
        return Math.min(BASE_CONE_HALF_ANGLE + (spellLevel * 3f), 36f);
    }

    private float getKnockback(int spellLevel, LivingEntity entity) {
        return 2.5f + (getSpellPower(spellLevel, entity) * 0.3f);
    }

    private float getBlockDestroyChance(int spellLevel, LivingEntity entity) {
        // Base chance + scaling with spell power, capped at 0.9 (90%)
        return Math.min(BASE_BLOCK_DESTROY_CHANCE + (getSpellPower(spellLevel, entity) * 0.03f), 0.9f);
    }

    private float getBlockFlingChance(int spellLevel, LivingEntity entity) {
        // Base chance + scaling with spell level, capped at 0.5 (50%)
        return Math.min(BASE_BLOCK_FLING_CHANCE + (spellLevel * 0.08f), 0.3f);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return FSpellAnimations.ANIMATION_POINT;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_INSTANT_CAST;
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }
}
