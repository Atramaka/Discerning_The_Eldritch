package net.feshy.cursed_sorcery.spells.curse;


import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.entity.spells.slice.Slice;
import net.feshy.cursed_sorcery.registries.DTESchoolRegistry;
import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SliceSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "slice");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.distance", getRange(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.max_charges", getRecastCount(spellLevel, caster))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(DTESchoolRegistry.CURSE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(12)
            .build();

    public SliceSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 0; // Instant cast for recastable
        this.baseManaCost = 30;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(DTESoundRegistry.DISMANTLE_SLICE.get());
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 1 + spellLevel; // 3-7 slices depending on level
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        // Set up recasts if this is the first cast
        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(
                    new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 100, castSource, null), 
                    playerMagicData
            );
        }

        // Fire a single large slice projectile
        spawnSliceProjectile(level, spellLevel, entity);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnSliceProjectile(Level level, int spellLevel, LivingEntity caster) {
        Slice slice = new Slice(level, caster);

        // Position at eye height
        slice.setPos(caster.position().add(0, caster.getEyeHeight() - slice.getBoundingBox().getYsize() * 0.8f, 0));

        // Fire straight where the player is looking - no spread
        float pitch = caster.getXRot();
        float yaw = caster.getYHeadRot();

        // Shoot with no inaccuracy for precise aim
        slice.shootFromRotation(caster, pitch, yaw, 0, getProjectileSpeed(spellLevel), 0f);
        slice.setDamage(getDamage(spellLevel, caster));

        level.addFreshEntity(slice);

        // Play slice sound with slight pitch variation
        float sfxPitch = 0.9f + Utils.random.nextFloat() * 0.2f;
        caster.playSound(DTESoundRegistry.DISMANTLE_SLICE.get(), 2.5f, sfxPitch);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return 1 + getSpellPower(spellLevel, caster);
    }

    private float getProjectileSpeed(int spellLevel) {
        return 1.8f + (spellLevel * 0.1f); // Slightly faster at higher levels
    }

    private int getRange(int spellLevel, LivingEntity caster) {
        return (int) (10 + spellLevel * 2);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        float f = getRange(spellLevel, mob);
        return mob.distanceToSqr(target) > (f * f) * 1.2;
    }
}
