package net.acetheeldritchking.discerning_the_eldritch.effects;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.effect.CustomDescriptionMobEffect;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;

import io.redspace.ironsspellbooks.damage.ISSDamageTypes;

import net.acetheeldritchking.discerning_the_eldritch.registries.DTEPotionEffectRegistry;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTESoundRegistry;
import net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries;
import net.acetheeldritchking.discerning_the_eldritch.utils.IEntityDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import net.minecraft.world.level.block.state.BlockState;

import static io.redspace.ironsspellbooks.damage.ISSDamageTypes.EVOCATION_MAGIC;

public class DestructiveRampageEffect extends CustomDescriptionMobEffect {

    // Track boost state per player
    private static final Map<UUID, BoostData> boostDataMap = new HashMap<>();
    private static final int BOOST_DURATION = 50; // 1.5 seconds
    private static final int BOOST_COOLDOWN = 110; // 3 seconds
    private static final double BOOST_SPEED_MULTIPLIER = 2;

    private static class BoostData {
        int boostTimer = 0;
        int cooldownTimer = 0;
        boolean wasSprintPressed = false;
    }

    public DestructiveRampageEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4500);
    }

    /**
     * Called from the network packet handler to trigger a boost
     */
    public static void triggerBoost(Player player) {
        BoostData boostData = boostDataMap.computeIfAbsent(player.getUUID(), k -> new BoostData());
        
        if (boostData.cooldownTimer <= 0 && boostData.boostTimer <= 0) {
            boostData.boostTimer = BOOST_DURATION;
            boostData.cooldownTimer = BOOST_COOLDOWN;

            Level level = player.level();
            if (!level.isClientSide && level instanceof ServerLevel sl) {
                MagicManager.spawnParticles(sl, new BlastwaveParticleOptions(new Vector3f(1.0f, 1.0f, 1.0f), 5f), player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0, true);
                sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
                sl.sendParticles(ParticleTypes.GUST, player.getX(), player.getY(), player.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5f, 1.8f);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
        }
    }

    @Override
    public Component getDescriptionLine(MobEffectInstance instance) {
        return Component.translatable("tooltip.discerning_the_eldritch.conquerors_flight_description");
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (!(living instanceof Player player)) return true;

        Level level = player.level();
        Vec3 look = player.getLookAngle().normalize();

        // Get or create boost data for this player
        BoostData boostData = boostDataMap.computeIfAbsent(player.getUUID(), k -> new BoostData());

        // Update cooldown timer
        if (boostData.cooldownTimer > 0) {
            boostData.cooldownTimer--;
        }

        // Update boost timer
        if (boostData.boostTimer > 0) {
            boostData.boostTimer--;
        }

        // 0. IMMEDIATE CROUCH CANCEL
        if (player.isShiftKeyDown()) {
            // 1. Kill the effect immediately
            player.removeEffect(DTEPotionEffectRegistry.DESTRUCTIVE_RAMPAGE_EFFECT);

            // 2. Force Physics & Animation Reset
            player.setPose(Pose.STANDING);
            ((IEntityDataAccessor) player).dte$setFallFlying(false);
            player.resetFallDistance();

            // 3. Apply downward slam impulse
            player.setDeltaMovement(0, -1.0, 0);
            player.hasImpulse = true;

            // 4. Cool "Slam Start" FX
            if (!level.isClientSide && level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.GUST, player.getX(), player.getY(), player.getZ(), 1, 0.2, 0.2, 0.2, 0.0);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.4f);
            }

            // 5. IMPORTANT: Return false here so NONE of the code below (Destruction/Flight) runs
            return false;
        }

        // BOOST ACTIVATION: Sprint key pressed
        boolean sprintPressed = player.isSprinting();
        if (sprintPressed && !boostData.wasSprintPressed && boostData.cooldownTimer <= 0 && boostData.boostTimer <= 0) {
            // Activate boost
            boostData.boostTimer = BOOST_DURATION;
            boostData.cooldownTimer = BOOST_COOLDOWN;

            // Sound barrier break VFX
            if (!level.isClientSide && level instanceof ServerLevel sl) {
                // Sonic boom particle ring
                MagicManager.spawnParticles(sl, 
                    new BlastwaveParticleOptions(new Vector3f(1.0f, 1.0f, 1.0f), 5f), 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    1, 0, 0, 0, 0, true);
                
                // Additional particles for effect
                sl.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 1, player.getZ(), 
                    1, 0.1, 0.1, 0.1, 0.0);
                
                sl.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 1, player.getZ(), 
                    1, 0.2, 0.2, 0.2, 0.0);
                
                // Sound barrier break sound
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 4f, 1f);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
        }
        boostData.wasSprintPressed = sprintPressed;

        // 1. STABILITY & ANIMATION
        player.setPose(Pose.FALL_FLYING);
        ((IEntityDataAccessor) player).dte$setFallFlying(true);
        double penetrationSlowdown = 1.0;
        // 2. PROACTIVE DESTRUCTION (Clearing the path ahead to prevent collision stutter)
        if (!level.isClientSide && level instanceof ServerLevel sl) {
            // Sweep a box slightly larger than the player in the direction of movement
            Vec3 movement = player.getDeltaMovement();
            double sweepDist = Math.max(2.5, movement.length() * 1.5);
            Vec3 sweepVec = look.scale(sweepDist);

            // Expand bounding box slightly for "breathing room"
            AABB sweepZone = player.getBoundingBox().expandTowards(sweepVec).inflate(0.5);

            float radius = 2.5f + (amplifier * 0.2f); // Tunnel width
            BlockPos min = BlockPos.containing(sweepZone.minX, sweepZone.minY, sweepZone.minZ);
            BlockPos max = BlockPos.containing(sweepZone.maxX, sweepZone.maxY, sweepZone.maxZ);

            boolean brokeBlock = false;
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0 && state.getDestroySpeed(level, pos) < 50) {
                    // Spawn debris occasionally
                    if (level.random.nextFloat() < 0.15f) {
                        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state);
                        fallingBlock.setDeltaMovement(look.add(
                                (level.random.nextFloat() - 0.5) * 0.5,
                                (level.random.nextFloat() * 0.3),
                                (level.random.nextFloat() - 0.5) * 0.5
                        ).scale(0.4));
                        level.addFreshEntity(fallingBlock);
                    }
                    level.destroyBlock(pos, false, player);
                    brokeBlock = true;
                }
            }

            if (brokeBlock) {
                // Occasional "drilling" effects when passing through structures
                if (level.getGameTime() % 3 == 0) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, player.getX() + look.x * 2, player.getY() + 1 + look.y * 2, player.getZ() + look.z * 2, 1, 0.5, 0.5, 0.5, 0.05);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.5f);
                }

                // Damage entities in the tunnel
                List<Entity> targets = sl.getEntities(player, sweepZone.inflate(1.0));
                for (Entity target : targets) {
                    if (target instanceof LivingEntity livingTarget && !player.isAlliedTo(target)) {
                        livingTarget.hurt(DamageSources.get(level, EVOCATION_MAGIC), 2.0f + (amplifier * 1.0f));
                        livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(look.scale(0.8)));
                    }
                }
            }
        }

        // 3. FLIGHT ENGINE (Includes boost multiplier)
        double baseSpeed = 1.65D + (amplifier * 0.25D);
        double speedMultiplier = boostData.boostTimer > 0 ? BOOST_SPEED_MULTIPLIER : 1.0;
        double speed = baseSpeed * speedMultiplier;
        
        Vec3 currentVel = player.getDeltaMovement();
        Vec3 targetVel = look.scale(speed);

        // Extra particles during boost
        if (boostData.boostTimer > 0 && level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, 
                player.getX() - look.x * 0.5, 
                player.getY() + 1 - look.y * 0.5, 
                player.getZ() - look.z * 0.5, 
                3, 0.2, 0.2, 0.2, 0.0);
        }

        player.setDeltaMovement(new Vec3(
                Mth.lerp(0.2, currentVel.x, targetVel.x),
                Mth.lerp(0.2, currentVel.y, targetVel.y) + 0.08,
                Mth.lerp(0.2, currentVel.z, targetVel.z)
        ));
        player.fallDistance = 0;
        player.hasImpulse = true;

        return true;
    }

    @Override
    public void onEffectRemoved(LivingEntity living, int amplifier) {
        if (living instanceof Player player) {
            ((IEntityDataAccessor) player).dte$setFallFlying(false);
            player.setPose(Pose.STANDING);
            // Clean up boost data
            boostDataMap.remove(player.getUUID());
        }
        super.onEffectRemoved(living, amplifier);
    }
}
