package net.acetheeldritchking.discerning_the_eldritch.spells.dark;


import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTESchoolRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;



import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class DecimateSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "decimate");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.distance", getRange(spellLevel, caster))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(DTESchoolRegistry.VOID_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(16)
            .build();

    public DecimateSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
        this.castTime = 60;  // 3 seconds cast time
        this.baseManaCost = 50;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.EARTHQUAKE_CAST.get());
    }

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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int range = getRange(spellLevel, entity);
        float damage = getDamage(spellLevel, entity);
        Vec3 forward = entity.getLookAngle().multiply(1, 0, 1).normalize();

        if (level instanceof ServerLevel serverLevel) {
            // We iterate along the length of the spell to ensure rotation alignment
            for (float i = 1.5f; i < range; i += 0.75f) {
                Vec3 segmentPos = entity.position().add(forward.scale(i));
                Vec3 spawn = Utils.moveToRelativeGroundLevel(level, segmentPos, 2);
                BlockPos bpos = BlockPos.containing(spawn.subtract(0, 0.1, 0));
                var blockState = level.getBlockState(bpos.below());

                // 1. DISCRETE BLOCK PARTICLES (aligned per segment)
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.DUST_PILLAR, blockState),
                        spawn.x, spawn.y, spawn.z, 2, 0.4, -0.08, 0.4, 0);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                        spawn.x, spawn.y, spawn.z, 5, 0.4, -0.08, 0.4, 0);

                // 2. ALIGNED DECAY FX (aligned per segment)
                serverLevel.sendParticles(ParticleTypes.ASH, spawn.x, spawn.y, spawn.z, 45, 0.5, 0.4, 0.5, 0.1);
                serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, spawn.x, spawn.y, spawn.z, 3, 0.3, 0.05, 0.3, 0.01);
                serverLevel.sendParticles(ParticleTypes.SQUID_INK, spawn.x, spawn.y, spawn.z, 3, 0.3, 0.05, 0.3, 0);
                // 3. SEGMENTED DAMAGE (Better alignment than a single large box)
                AABB segmentArea = new AABB(spawn.x - 1.2, spawn.y - 1, spawn.z - 1.2, spawn.x + 1.2, spawn.y + 1.5, spawn.z + 1.2);
                List<Entity> targets = level.getEntities(entity, segmentArea);

                for (Entity target : targets) {
                    if (target instanceof LivingEntity livingTarget && livingTarget != entity && !entity.isAlliedTo(livingTarget)) {
                        // Instant Decay Hit (Damage scales per segment hit if they stay in it)
                        livingTarget.hurt(level.damageSources().magic(), damage / 6f);

                        // Serious DPS (Blood Rot) - High frequency feel
                        // Using a shorter duration but potentially reapplying to refresh "ticks"
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 30, spellLevel));
                    }
                }
            }
            // Play sound at the end of the line creation
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.SUNBEAM_IMPACT.get(), entity.getSoundSource(), 1.0f, 0.8f);
        }

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

    private float getDamage(int spellLevel, LivingEntity caster) {
        return 20 + getSpellPower(spellLevel, caster);
    }

    private int getRange(int spellLevel, LivingEntity caster) {
        return (int) (12 + spellLevel * getEntityPowerMultiplier(caster));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }



    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        float f = getRange(spellLevel, mob);
        return mob.distanceToSqr(target) > (f * f) * 1.2;
    }
}
