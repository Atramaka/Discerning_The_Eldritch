package net.acetheeldritchking.discerning_the_eldritch.spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.ImpulseCastData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTEPotionEffectRegistry;
import net.acetheeldritchking.discerning_the_eldritch.utils.IEntityDataAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ConquerorsFlightSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "conquerors_flight");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(45)
            .build();

    public ConquerorsFlightSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 20;
        this.baseManaCost = 100;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ELDRITCH_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FORCE_IMPACT.get());
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) { return true; }

    @Override
    public ICastDataSerializable getEmptyCastData() { return new ImpulseCastData(); }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (entity instanceof Player player) {
            // Initial burst of invisibility and lift
            //player.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, 8, 1, false, false, true));
            player.setPose(Pose.FALL_FLYING);
            ((IEntityDataAccessor) player).dte$setFallFlying(true);
            if (player.onGround()) {
                player.move(MoverType.SELF, new Vec3(0.0, 2, 0.0));
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerPositionPacket(0.0, 2, 0.0, 0, 0, RelativeMovement.ALL, player.getId()));
                }
            }

            Vec3 forward = player.getLookAngle().multiply(1, 0, 1).normalize();
            player.setDeltaMovement(new Vec3(forward.x * 1.5, 1.0, forward.z * 2));
            player.hasImpulse = true;

            // Apply Destructive Rampage Effect
            int duration = getDuration(spellLevel, entity);
            player.addEffect(new MobEffectInstance(DTEPotionEffectRegistry.DESTRUCTIVE_RAMPAGE_EFFECT, duration, spellLevel - 1, false, false, true));

            if (!level.isClientSide && level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, player.getX(), player.getY() + 0.5, player.getZ(), 100, 0.5, 0.5, 0.5, 0.2);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {

        if (entity instanceof Player player) {
            player.setPose(Pose.FALL_FLYING);
            ((IEntityDataAccessor) player).dte$setFallFlying(true);
        }

        // optional: keep this, it helps client feel immediate impulse
        if (castData instanceof ImpulseCastData impulse) {
            entity.hasImpulse = impulse.hasImpulse;
            entity.setDeltaMovement(entity.getDeltaMovement().add(impulse.x, impulse.y, impulse.z));
        }



        super.onClientCast(level, spellLevel, entity, castData);
    }

    private int getDuration(int spellLevel, LivingEntity caster) {
        return 160 + (spellLevel * 50); // 10 to 30 Seconds
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }
}
