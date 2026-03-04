package net.acetheeldritchking.discerning_the_eldritch.spells.dark;

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
import net.acetheeldritchking.discerning_the_eldritch.registries.DTESchoolRegistry;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class VoidSurfSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, "void_surf");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(30)
            .build();

    public VoidSurfSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 80;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }




    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.BLACK_HOLE_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.BLACK_HOLE_CAST.get());
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) { return true; }

    @Override
    public ICastDataSerializable getEmptyCastData() { return new ImpulseCastData(); }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        // optional: keep this, it helps client feel immediate impulse
        if (castData instanceof ImpulseCastData impulse) {
            entity.hasImpulse = impulse.hasImpulse;
            entity.setDeltaMovement(entity.getDeltaMovement().add(impulse.x, impulse.y, impulse.z));
        }
        super.onClientCast(level, spellLevel, entity, castData);
    }

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
            // Calculate Leap Impulse: Always UP and slightly FORWARD
            player.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, 12, 1, false, false, true));
            // Force player off ground (standard ISS technique to bypass friction)
            if (player.onGround()) {
                player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerPositionPacket(0.0, 1.2, 0.0, 0, 0, RelativeMovement.ALL, player.getId()));
                }
            }

            Vec3 forward = player.getLookAngle().multiply(1, 0, 1).normalize();
            Vec3 leapImpulse = new Vec3(forward.x * 1, .8, forward.z * 1);


            player.setDeltaMovement(leapImpulse);
            player.hasImpulse = true;
            player.hurtMarked = true;

            // Apply flight effect
            int duration = getDuration(spellLevel, entity);
            player.addEffect(new MobEffectInstance(DTEPotionEffectRegistry.VOID_SURF_EFFECT, duration, spellLevel, false, false, true));

            // DARK VFX BURST
            if (!level.isClientSide && level instanceof ServerLevel sl) {

                DustParticleOptions indicatorColor = new DustParticleOptions(new Vector3f(0.4f, 0.0f, 0.8f), 1.0f);
                sl.sendParticles(indicatorColor, player.getX(),player.getY() + 0.5, player.getZ(), 12, 0, 0, 0, 0.2);

                sl.sendParticles(ParticleTypes.SQUID_INK, player.getX(),player.getY() + 0.5, player.getZ(), 25, 0.4, 0.2, 0.4, 0.45);
               // sl.sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 0.5, player.getZ(), 40, 0.5, 0.5, 0.5, 0.05);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    private int getDuration(int spellLevel, LivingEntity caster) {
        return 60 + (spellLevel * 10); // 3 Seconds
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

}
