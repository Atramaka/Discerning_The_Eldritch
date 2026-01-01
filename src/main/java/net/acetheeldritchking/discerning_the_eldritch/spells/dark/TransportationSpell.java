package net.acetheeldritchking.discerning_the_eldritch.spells.dark;


import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTESchoolRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class TransportationSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "transportation");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(DTESchoolRegistry.DARK_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(5)
            .build();

    public TransportationSpell() {
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 5;
        this.baseManaCost = 40;
        this.manaCostPerLevel = 10;
        this.castTime = 30; // 1.5 second charge time
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
        return Optional.of(SoundEvents.WARDEN_SONIC_CHARGE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENDERMAN_TELEPORT);
    }
    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        // Decide destination the MOMENT charging starts and store it
        float maxDistance = getDistance(spellLevel, entity);
        Vec3 destination = findTeleportLocation(level, entity, maxDistance);
        playerMagicData.setAdditionalCastData(new TransportationData(destination));
        return true;
    }



    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TransportationData data) {
            if (level.getGameTime() % 4 == 0) {
                // Visual indicator at the TARGET location while charging
                spawnVoidIndicator(level, data.destination, 2.0f, false);
                // Visual indicator at caster's feet
                spawnVoidIndicator(level, entity.position(), 1.2f, false);
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        Vec3 startPos = entity.position();
        Vec3 dest;


        // Use the locked destination from charging, fallback if null
        if (playerMagicData.getAdditionalCastData() instanceof TransportationData data) {
            dest = data.destination;
        } else {
            // Fallback just in case
            dest = findTeleportLocation(level, entity, getDistance(spellLevel, entity));
        }

        float damage = getSpellPower(spellLevel, entity) * 0.5f;

        // 1. Effects and Damage at START position
        spawnVoidBlast(level, startPos);
        spawnSkyBeam(level, startPos);
        dealAOEDamage(level, entity, startPos, 3.5f, damage);

        // 2. Perform Teleport
        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        Utils.handleSpellTeleport(this, entity, dest);
        entity.resetFallDistance();

        // 3. Effects and Damage at DESTINATION position
        spawnVoidBlast(level, dest);
        spawnSkyBeam(level, dest);
        dealAOEDamage(level, entity, dest, 3.5f, damage);

        entity.playSound(getCastFinishSound().get(), 2.0f, 0.8f);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnVoidIndicator(Level level, Vec3 pos, float radius, boolean isFull) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int count = isFull ? 60 : 25;
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = pos.x + radius * Math.cos(angle);
            double z = pos.z + radius * Math.sin(angle);

            // Use purple dust for the target indicator to make it visible
            DustParticleOptions indicatorColor = new DustParticleOptions(new Vector3f(0.4f, 0.0f, 0.8f), 1.0f);
            serverLevel.sendParticles(indicatorColor, x, pos.y + 0.2, z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, x, pos.y + 0.1, z, 1, 0, 0, 0, 0);
        }
    }



    private void dealAOEDamage(Level level, LivingEntity caster, Vec3 pos, float radius, float damage) {
        level.getEntities(caster, caster.getBoundingBox().move(pos.subtract(caster.position())).inflate(radius), (target) -> !DamageSources.isFriendlyFireBetween(target, caster)).forEach(target -> {
            if (target instanceof LivingEntity livingTarget && target != caster && target.distanceToSqr(pos) < radius * radius) {
                DamageSources.applyDamage(livingTarget, damage, getDamageSource(caster));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        });
    }
    private void spawnVoidBlast(Level level, Vec3 pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        DustParticleOptions purpleDust = new DustParticleOptions(new Vector3f(0.3f, 0f, 0.6f), 1.8f);

        // Slightly bigger circular shockwave
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(new Vector3f(0.1f, 0f, 0.2f), 3.5f), pos.x, pos.y + 0.1, pos.z, 1, 0, 0, 0, 0, true);

        // Blast particles
        serverLevel.sendParticles(purpleDust, pos.x, pos.y + 1, pos.z, 30, 0.6, 0.6, 0.6, 0.1);
        serverLevel.sendParticles(ParticleTypes.SQUID_INK, pos.x, pos.y + 1, pos.z, 20, 0.4, 0.4, 0.4, 0.05);
        serverLevel.sendParticles(ParticleTypes.WITCH, pos.x, pos.y + 1, pos.z, 10, 0.5, 0.5, 0.5, 0.1);
    }

    private void spawnSkyBeam(Level level, Vec3 pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        DustParticleOptions beamParticle = new DustParticleOptions(new Vector3f(0.1f, 0f, 0.3f), 2.0f);
        
        // Create a vertical beam from the sky (y=30 above player) down to ground
        for (int i = 0; i < 40; i++) {
            double yOffset = i * 0.75;
            serverLevel.sendParticles(beamParticle, pos.x, pos.y + yOffset, pos.z, 2, 0.1, 0.1, 0.1, 0.0);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, pos.x, pos.y + yOffset, pos.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }


    public static class TransportationData implements ICastData {
        public final Vec3 destination;

        public TransportationData(Vec3 destination) {
            this.destination = destination;
        }

        @Override
        public void reset() {}
    }

    public static Vec3 findTeleportLocation(Level level, LivingEntity entity, float maxDistance) {
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, maxDistance);
        BlockPos pos = blockHitResult.getBlockPos();
        // Simple destination solver
        return new Vec3(pos.getX() + 0.5, blockHitResult.getLocation().y + 0.1, pos.getZ() + 0.5);
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return getSpellPower(spellLevel, sourceEntity) * 1.0f;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(spellLevel, caster), 1)));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }
}
