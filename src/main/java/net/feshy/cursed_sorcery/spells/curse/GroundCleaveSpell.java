package net.feshy.cursed_sorcery.spells.curse;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.registries.DTESchoolRegistry;

import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class GroundCleaveSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "ground_cleave");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(DTESchoolRegistry.CURSE_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(15)
            .build();

    public GroundCleaveSpell()
    {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 120;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public boolean canBeCraftedBy(Player player) {
        return false;
    }

    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public boolean allowCrafting() {
        return false;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.WITHER_AMBIENT);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(DTESoundRegistry.DISMANTLE_SLICE.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CAST_KNEELING_PRAYER;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        float radius = 3.25f;
        float distance = 1.9f;

        Vec3 forward = entity.getForward();
        Vec3 hitLocation = entity.position()
                .add(0, entity.getBbHeight() * .3f, 0)
                .add(forward.scale(distance));

        var entities = level.getEntities(entity, AABB.ofSize(hitLocation, radius * 2, radius, radius * 2));

        // Effect tuning
        int effectDurationTicks = 60 + spellLevel * 20;   // 3s @ lvl1, scales up
        int effectAmplifier = Math.max(0, spellLevel - 1); // lvl1->amp0, lvl2->amp1, etc.
        float knockbackStrength = 0.8f + 0.2f * spellLevel;

        for (Entity targetEntity : entities) {
            if (targetEntity instanceof LivingEntity livingTarget
                    && targetEntity.isAlive()
                    && targetEntity.isPickable()
                    && targetEntity.position().subtract(entity.getEyePosition()).dot(forward) >= 0
                    && entity.distanceToSqr(targetEntity) < radius * radius
                    && Utils.hasLineOfSight(level, entity.getEyePosition(), targetEntity.getBoundingBox().getCenter(), true)) {

                Vec3 offsetVector = targetEntity.getBoundingBox().getCenter().subtract(entity.getEyePosition());
                if (offsetVector.dot(forward) >= 0) {
                    // Knockback away from caster
                    Vec3 away = livingTarget.position().subtract(entity.position());
                    double dx = away.x;
                    double dz = away.z;
                    if (dx * dx + dz * dz < 1.0E-4) {
                        dx = forward.x;
                        dz = forward.z;
                    }
                    livingTarget.knockback(knockbackStrength, dx, dz);

                    // Apply Cleave effect (handles rapid particles + rapid damage in the effect class)

//                    livingTarget.addEffect(new MobEffectInstance(
//                            DTEPotionEffectRegistry.CLEAVE_POTION_EFFECT,
//                            effectDurationTicks,
//                            effectAmplifier,
//                            true,   // ambient
//                            true,   // showParticles (keep true; your effect also spawns particles server-side)
//                            true    // showIcon
//                    ));
                }
            }
        }

        // Ground disintegration effect - spherical crater below the player
        if (level instanceof ServerLevel serverLevel) {
            disintegrateGround(serverLevel, entity, spellLevel);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /**
     * Disintegrates the ground in a spherical pattern below the player,
     * creating a crater 1 block thick with dust and smoke particle effects.
     * Damages entities caught within the destruction radius.
     */
    private void disintegrateGround(ServerLevel level, LivingEntity caster, int spellLevel) {
        Vec3 center = caster.position().add(0, 0.5, 0); // Center at player's feet level
        float craterRadius = 3.5f + spellLevel * 0.5f; // Radius scales with spell level
        float damage = getDamage(spellLevel, caster) * 2.5f; // Major damage multiplier

        // Play explosive sound
        level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 0.6f);
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 1.2f);

        // Damage entities in the crater radius (excluding caster)
        AABB damageBox = new AABB(
                center.x - craterRadius, center.y - 1, center.z - craterRadius,
                center.x + craterRadius, center.y + 5, center.z + craterRadius
        );

        for (Entity target : level.getEntities(caster, damageBox)) {
            if (target instanceof LivingEntity livingTarget && !DamageSources.isFriendlyFireBetween(target, caster)) {
                double distSq = target.position().distanceToSqr(center.x, center.y, center.z);
                if (distSq <= craterRadius * craterRadius) {
                    // Apply damage with falloff based on distance
                    float distanceFactor = 1.0f - (float) (Math.sqrt(distSq) / craterRadius) * 0.5f;
                    DamageSources.applyDamage(livingTarget, damage * distanceFactor, getDamageSource(caster));

                    // Strong knockback upward and outward
                    Vec3 knockDir = livingTarget.position().subtract(center).normalize();
                    livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(
                            knockDir.x * 1.5, 0.8 + Math.random() * 0.4, knockDir.z * 1.5
                    ));
                    livingTarget.hurtMarked = true;
                }
            }
        }

        // Destroy blocks in a spherical pattern
        BlockPos centerPos = BlockPos.containing(center);
        int radiusInt = (int) Math.ceil(craterRadius);

        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int z = -radiusInt; z <= radiusInt; z++) {
                // Check if this x,z position is within the circular radius
                double horizontalDistSq = x * x + z * z;
                if (horizontalDistSq > craterRadius * craterRadius) continue;

                // Ground layer (y = -1 relative to center) - fully destroyed with particles
                BlockPos groundPos = centerPos.offset(x, -1, z);
                BlockState groundState = level.getBlockState(groundPos);

                if (!groundState.isAir() && groundState.getDestroySpeed(level, groundPos) >= 0) {
                    spawnDisintegrationParticles(level, groundPos);
                    level.destroyBlock(groundPos, false);
                }

                // Layers above (y = 0 to 3 relative to center) - become falling blocks
                for (int y = 0; y <= 3; y++) {
                    BlockPos abovePos = centerPos.offset(x, y, z);
                    BlockState aboveState = level.getBlockState(abovePos);

                    if (!aboveState.isAir() && aboveState.getDestroySpeed(level, abovePos) >= 0) {
                        // Spawn particles at original position
                        spawnDisintegrationParticles(level, abovePos);

                        // Remove the block first
                        level.removeBlock(abovePos, false);

                        // Create falling block entity
                        FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, abovePos, aboveState);
                        fallingBlock.time = 1; // Prevent immediate placement
                        fallingBlock.dropItem = true; // Don't drop as item

                        fallingBlock.setHurtsEntities(0f, 0); // Don't hurt entities on landing

                        // Give it a slight outward velocity for dramatic effect
                        double outwardX = x * 0.05;
                        double outwardZ = z * 0.05;
                        fallingBlock.setDeltaMovement(outwardX, 0.1 + Math.random() * 0.1, outwardZ);

                        level.addFreshEntity(fallingBlock);
                    }
                }
            }
        }
    }

    /**
     * Spawns dust and campfire smoke particles to simulate block disintegration
     */
    private void spawnDisintegrationParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Gray/brown dust particles to simulate debris
        DustParticleOptions grayDust = new DustParticleOptions(
                new Vector3f(0.4f, 0.35f, 0.3f), 1.5f);
        DustParticleOptions darkDust = new DustParticleOptions(
                new Vector3f(0.2f, 0.18f, 0.15f), 2.0f);

        // Spawn dust particles bursting outward
        for (int i = 0; i < 4; i++) {
            double dx = (Math.random() - 0.5) * 0.3;
            double dy = Math.random() * 0.2 + 0.1;
            double dz = (Math.random() - 0.5) * 0.3;

            level.sendParticles(grayDust, x, y, z, 1,
                    0.3, 0.3, 0.3, 0.05);
            level.sendParticles(darkDust, x, y, z, 1,
                    0.2, 0.2, 0.2, 0.03);
        }

        // Campfire smoke rising up - creates the "disintegration" look
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y, z, 2,
                0.2, 0.1, 0.2, 0.01);

        // Additional smoke effect
        level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                x, y + 0.3, z, 1,
                0.15, 0.05, 0.15, 0.005);

        // Poof particles for extra debris feel
        level.sendParticles(ParticleTypes.POOF,
                x, y, z, 2,
                0.25, 0.25, 0.25, 0.02);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTicks(0).setLifestealPercent(0.25F);
    }

    private float getDamage(int spellLevel, LivingEntity caster)
    {
        return ( (getSpellPower(spellLevel, caster) * 2f ) + getWeaponDamage(caster));
    }

    private float getWeaponDamage(LivingEntity caster)
    {
        float weaponDamage = Utils.getWeaponDamage(caster);

        return weaponDamage;
    }

    private float getRadius(int spellLevel, LivingEntity entity) {
        // Base radius of 4, scales with level and spell power
        return 1f + (getSpellPower(spellLevel, entity) * 0.3f);
    }

    private String getDamageText(int spellLevel, LivingEntity caster)
    {
        if (caster != null)
        {
            float weaponDamage = Utils.getWeaponDamage(caster);
            String plus = "";
            if (weaponDamage > 0)
            {
                plus = String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1));
            }
            String damage = Utils.stringTruncation(getDamage(spellLevel, caster), 1);
            return damage + plus;
        }
        return "" + getSpellPower(spellLevel, caster);
    }
}
