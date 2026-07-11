package net.feshy.cursed_sorcery.spells.curse;


import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.entity.spells.dismantle.Dismantle;
import net.feshy.cursed_sorcery.registries.DTESchoolRegistry;
import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class DismantleSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "dismantle");

    // Barrage tuning
    private static final int SHOT_INTERVAL_TICKS = 2; // 10 shots/sec
    private static final float YAW_SPREAD_DEGREES = 14f;
    private static final float PITCH_SPREAD_DEGREES = 8f;
    private static final float PROJECTILE_INACCURACY = 0.35f;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.distance", getRange(spellLevel, caster))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(DTESchoolRegistry.CURSE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(16)
            .build();

    public DismantleSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 120;  // 3 seconds cast time
        this.baseManaCost = 50;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(DTESoundRegistry.DISMANTLE_SLICE.get());
    }

//    @Override
//    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
//        return 3 + spellLevel*3;
//    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return true;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return getCastTime(spellLevel);
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
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData playerMagicData) {
        // ... existing code ...

        if (level.isClientSide) {
            super.onServerCastTick(level, spellLevel, caster, playerMagicData);
            return;
        }


        // Fire a rapid barrage while continuously casting
        if (level.getGameTime() % SHOT_INTERVAL_TICKS == 0) {
            spawnDismantleShot(level, spellLevel, caster);
        }

        super.onServerCastTick(level, spellLevel, caster, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        // Continuous spell now fires during onServerCastTick, so don't also fire here (prevents double-shooting).

//        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
//            playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 80, castSource, null), playerMagicData);
//        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        // Apply slowness during casting
        if (entity instanceof Player player && !level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, getCastTime(spellLevel), 2, false, false, true));
        }
        super.onClientCast(level, spellLevel, entity, castData);
    }

    private void spawnDismantleShot(Level level, int spellLevel, LivingEntity caster) {
        Dismantle dismantle = new Dismantle(level, caster);

        dismantle.setPos(caster.position().add(0, caster.getEyeHeight() - dismantle.getBoundingBox().getYsize() * .5f, 0));

        float basePitch = caster.getXRot();
        float baseYaw = caster.getYHeadRot();

        float yawOffset = (Utils.random.nextFloat() * 2f - 1f) * YAW_SPREAD_DEGREES;
        float pitchOffset = (Utils.random.nextFloat() * 2f - 1f) * PITCH_SPREAD_DEGREES;

        dismantle.shootFromRotation(caster, basePitch + pitchOffset, baseYaw + yawOffset, 0, dismantle.getSpeed(), PROJECTILE_INACCURACY);
        dismantle.setDamage(getDamage(spellLevel, caster));
        // Play slice SFX per projectile with varying pitch (0.85 -> 1.1)

        float sfxPitch = 0.85f + Utils.random.nextFloat() * 0.25f;
        level.playSound(
                null, // null player means all players hear it
                caster.getX(),
                caster.getY(),
                caster.getZ(),
                DTESoundRegistry.DISMANTLE_SLICE.get(),
                SoundSource.PLAYERS,
                1.5f, // volume
                sfxPitch
        );

        level.addFreshEntity(dismantle);


    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return (int) (3 +  (spellLevel *.5f));
    }

    private int getRange(int spellLevel, LivingEntity caster) {
        return (int) (8 + spellLevel * getEntityPowerMultiplier(caster));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST;
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        float f = getRange(spellLevel, mob);
        return mob.distanceToSqr(target) > (f * f) * 1.2;
    }
}
