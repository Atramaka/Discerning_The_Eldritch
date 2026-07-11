package net.feshy.cursed_sorcery.spells.curse;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.entity.spells.lapse_blue.LapseBlueEntity;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.feshy.cursed_sorcery.registries.DTESchoolRegistry;
import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.feshy.cursed_sorcery.utils.FSpellAnimations;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class BlueSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "lapse_blue");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(DTESchoolRegistry.CURSE_RESOURCE) // Change to your desired school
            .setMaxLevel(5)
            .setCooldownSeconds(60)
            .build();

    public BlueSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 60; // 3 seconds charge time
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
        // Use your own sound or one from ISS
        return Optional.of(SoundRegistry.TELEKINESIS_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(DTESoundRegistry.LAPSE_BLUE_SUMMON.get());
    }


    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {

        if (entity.hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT)) {
            // Apply slow falling effect to the caster
            entity.addEffect(new MobEffectInstance(
                    DTEPotionEffectRegistry.FLOAT_EFFECT,
                    this.castTime + getDuration(spellLevel, entity), // Duration in ticks (10 seconds)
                    0,   // Amplifier (level)
                    false, // Ambient
                    true   // Show particles
            ));
        }

        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);

        // Spawn blue dust particles in a small circle above the player every tick
        if (level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = entity.position();
            double heightAbove = entity.getBbHeight() + 0.5; // Slightly above head

            Vec3 eyePos = entity.getEyePosition();
            Vec3 lookVec = entity.getLookAngle();
            Vec3 pos = eyePos.add(lookVec.scale(1.0));

            // Blue dust particle options
            DustParticleOptions blueDust = new DustParticleOptions(
                    new Vector3f(0.04f, 0.78f, 1.0f), // Blue color (RGB)
                    1.0f // Size
            );

            // Rotating circle effect based on game time
            long gameTime = level.getGameTime();
            double rotationOffset = (gameTime % 20) * (Math.PI * 2 / 20); // Full rotation every 2 seconds

            // Spawn particles in a small rotating circle
            int particleCount = 5;
            double radius = 0.2;
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount + rotationOffset;
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y;
                double z = pos.z + Math.sin(angle) * radius;

                serverLevel.sendParticles(blueDust, x, y, z, 1, 0, 0, 0, 0);
            }

            // Add some random sparkle particles within the circle (every few ticks to avoid spam)
            if (gameTime % 2 == 0) {
                for (int i = 0; i < 2; i++) {
                    double offsetX = (Math.random() - 0.5) * radius * 2;
                    double offsetZ = (Math.random() - 0.5) * radius * 2;
                    serverLevel.sendParticles(blueDust,
                            pos.x + offsetX,
                            pos.y + (Math.random() - 0.5) * 0.3,
                            pos.z + offsetZ,
                            1, 0, 0.01, 0, 0);
                }
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = getRadius(spellLevel, entity);

        // Raycast to find where to place the blue sphere
        HitResult raycast = Utils.raycastForEntity(level, entity, 0.5f + radius * 1.2f, true);
        Vec3 center = raycast.getLocation();

        // Adjust position based on where we hit
        if (raycast instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getDirection().getAxis().isHorizontal()) {
                // Hit a wall - center the sphere on the hit point
                Vec3 eyePos = entity.getEyePosition();
                Vec3 lookVec = entity.getLookAngle();
                center = eyePos.add(lookVec.scale(2.0 + spellLevel)); // 2 blocks in front of where player is looking
            } else if (blockHitResult.getDirection() == Direction.DOWN) {
                // Hit ceiling - hang from it
                center = center.subtract(0, radius - 0.5, 0);
            } else {
                // Hit floor - float above it
                center = center.add(0, radius * 0.3, 0);
            }
        }
        else {
            // No block hit - spawn 2 blocks in front of the player at eye level
            Vec3 eyePos = entity.getEyePosition();
            Vec3 lookVec = entity.getLookAngle();
            center = eyePos.add(lookVec.scale(4.0)); // 2 blocks in front of where player is looking
        }



        this.playSound(getCastFinishSound(), entity);

        LapseBlueEntity lapseBlue = new LapseBlueEntity(level, entity);
        lapseBlue.setCaster(entity);
        lapseBlue.setAttractionRadius(radius);
        lapseBlue.setDamage(getDamage(spellLevel, entity));
        lapseBlue.setDuration(getDuration(spellLevel, entity));
        lapseBlue.setAttractionStrength(getAttractionStrength(spellLevel, entity));
        lapseBlue.setBlockPullChance(getBlockPullChance(spellLevel, entity));
        lapseBlue.setExplosionDamageMultiplier(getExplosionDamageMultiplier(spellLevel, entity));
        lapseBlue.setExplosionKnockback(getExplosionKnockback(spellLevel, entity));
        lapseBlue.setDistances(getStartingDistance(spellLevel, entity), getMaxDistance(spellLevel, entity));
        lapseBlue.setModelScale(getModelScale(spellLevel, entity));
        lapseBlue.moveTo(center);
        level.addFreshEntity(lapseBlue);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity)*0.3f;
    }

    private float getRadius(int spellLevel, LivingEntity entity) {
        // Base radius of 4, scales with level and spell power
        return 1.4f + (getSpellPower(spellLevel, entity) * 0.3f);
    }

    private int getDuration(int spellLevel, LivingEntity entity) {
        // Base duration of 3 seconds, +1 second per level
        return (40 + (spellLevel * 15));
    }



    private float getAttractionStrength(int spellLevel, LivingEntity entity) {
        // Scales with spell power: base 0.1 + 0.05 per spell power
        return 0.3f + (getSpellPower(spellLevel, entity) * 0.02f);
    }

    private float getBlockPullChance(int spellLevel, LivingEntity entity) {
        // Scales with spell power: base 0.05 (5%) + 0.03 per spell power
        // Capped at 0.5 (50%) to prevent too many blocks
        return Math.min(0.25f + (getSpellPower(spellLevel, entity) * 0.05f), 0.6f);
    }

    private float getExplosionDamageMultiplier(int spellLevel, LivingEntity entity) {
        // Scales with spell power: base 1.5x + 0.3x per spell power
        return 1.5f + (getSpellPower(spellLevel, entity) * 0.4f);
    }

    private float getExplosionKnockback(int spellLevel, LivingEntity entity) {
        // Scales with spell power: base 1.5 + 0.2 per spell power
        return 1.2f + (getSpellPower(spellLevel, entity) * 0.2f);
    }

    private float getStartingDistance(int spellLevel, LivingEntity entity) {
        // Base 2 blocks + 0.5 blocks per level
        return 3.5f + (spellLevel * 0.6f);
    }

    private float getMaxDistance(int spellLevel, LivingEntity entity) {
        // Base 10 blocks + 1 block per level
        return 10.0f + (spellLevel * 0.75f);
    }

    private float getModelScale(int spellLevel, LivingEntity entity) {
        // Base scale 2.0 + 0.2 per level
        return 0.8f + (spellLevel * 0.55f);
    }


    @Override
    public AnimationHolder getCastStartAnimation() {
        return FSpellAnimations.ANIMATION_BLUE_SUMMON;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.FINISH_ANIMATION;
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }
}
