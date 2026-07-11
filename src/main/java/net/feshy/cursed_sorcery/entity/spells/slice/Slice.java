package net.feshy.cursed_sorcery.entity.spells.slice;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import net.feshy.cursed_sorcery.registries.DTEEntityRegistry;
import net.feshy.cursed_sorcery.registries.SpellRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Slice extends AbstractMagicProjectile implements AntiMagicSusceptible {
    private final List<Entity> entities = new ArrayList<>();

    // Shorter lifetime so slashes despawn sooner (less distance traveled overall)
    private int lifetimeInTicks = 35;


    // Block breaking configuration
    private static final double BLOCK_BREAK_RADIUS = 1.0; // Radius around projectile to check for blocks
    private static final double SLASH_LINE_LENGTH = 1; // Length of the slash line (3 blocks long)
    private static final double SLASH_LINE_WIDTH = .1; // Width of the slash line
    private static final double SLASH_LINE_HEIGHT = .1; // Height of the slash line
    private static final int BLOCK_CHECK_INTERVAL = 1; // Check every tick


    public Slice(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);

        this.setNoGravity(true);
    }

    public Slice(EntityType<? extends Projectile> entityType, Level level, LivingEntity shooter)
    {
        this(entityType, level);
        setOwner(shooter);
        setYRot(shooter.getYRot());
        setXRot(shooter.getXRot());
    }

    public Slice(Level level, LivingEntity shooter)
    {
        this(DTEEntityRegistry.SLICE.get(), level, shooter);
    }

    @Override
    public void travel() {
        this.setPos(this.position().add(this.getDeltaMovement()));
        if (!this.isNoGravity())
        {
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x, vec3.y - 0.035, vec3.z);
        }
    }

    @Override
    public void tick() {
        lifetimeInTicks--;
        if (lifetimeInTicks <= 0)
        {
            this.discard();
        }

        if (!level().isClientSide)
        {
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() == HitResult.Type.BLOCK) {
                onHitBlock((BlockHitResult) hitresult);
            }
            for (Entity entity : level().getEntities(this, this.getBoundingBox()).stream().filter(target -> canHitEntity(target) && !entities.contains(target)).collect(Collectors.toSet())) {
                damageEntity(entity);
            }

            // Check for and break blocks in the projectile's path
            if (tickCount % BLOCK_CHECK_INTERVAL == 0) {
                breakBlocksInPath();
            }
        }

        Vec3 deltaMovement = getDeltaMovement();
        double distance = deltaMovement.horizontalDistance();

        double x = deltaMovement.x;
        double y = deltaMovement.y;
        double z = deltaMovement.z;

        setYRot((float) (Mth.atan2(x, z) * (180 / Math.PI)));
        setXRot((float) (Mth.atan2(y, distance) * (180 / Math.PI)));
        setXRot(lerpRotation(xRotO, getXRot()));
        setYRot(lerpRotation(yRotO, getYRot()));

        super.tick();
    }

    /**
     * Breaks blocks in the projectile's path based on block hardness
     * Uses the same hardness rules as the vortex: skips unbreakable and very hard blocks
     */


    private void breakBlocksInPath() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        Vec3 center = this.position();

        // Get the projectile's yaw (horizontal rotation) to determine the slash line direction
        double yaw = Math.toRadians(this.getYRot());

        // Forward direction based on yaw (direction the slash is moving)
        double forwardX = Math.sin(yaw);
        double forwardZ = Math.cos(yaw);

        // Right direction (perpendicular to forward, for the width of the slash)
        double rightX = Math.sin(yaw + Math.PI / 2);
        double rightZ = Math.cos(yaw + Math.PI / 2);

        // Collect all blocks to destroy in this frame
        java.util.Set<BlockPos> blocksToDestroy = new java.util.HashSet<>();
        boolean shouldDiscard = false;

        // Extended slash pattern: 3 blocks forward and back, 1.5 blocks wide
        // This creates a horizontal line of destruction
        for (double forwardDist = -1; forwardDist <= 1; forwardDist += 0.5) {
            for (double rightDist = -1.0; rightDist <= 1.0; rightDist += 0.5) {
                for (double yOffset = -0.5; yOffset <= 0.5; yOffset++) {
                    // Calculate position along the slash line
                    double checkX = center.x + forwardX * forwardDist + rightX * rightDist;
                    double checkY = center.y + yOffset;
                    double checkZ = center.z + forwardZ * forwardDist + rightZ * rightDist;

                    BlockPos checkPos = BlockPos.containing(checkX, checkY, checkZ);

                    // Avoid checking the same block multiple times
                    if (blocksToDestroy.contains(checkPos)) continue;

                    net.minecraft.world.level.block.state.BlockState blockState = level().getBlockState(checkPos);

                    // Skip air blocks
                    if (blockState.isAir()) continue;

                    // Get block hardness
                    float blockHardness = blockState.getDestroySpeed(level(), checkPos);

                    // Mark for discard if we hit an unbreakable or very hard block
                    if (blockHardness < 0 || blockHardness >= 1.5f) {
                        shouldDiscard = true;
                        continue;
                    }

                    // Add to collection for destruction
                    blocksToDestroy.add(checkPos);
                }
            }
        }

        // Now destroy all collected blocks and spawn particles
        for (BlockPos blockPos : blocksToDestroy) {
            // Break the block without drops
            level().destroyBlock(blockPos, false);

            // Spawn poof particles at block position
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                    6, 0.3, 0.3, 0.3, 0.12
            );
        }

        // Discard after destroying all breakable blocks
        if (shouldDiscard) {
            this.discard();
        }
    }

    @Override
    public void trailParticles() {
        for (int i = 0; i < 3; i++)
        {
            // Middle
            double speed = 0.05F;
            double dx = Math.random() * 2 * speed - speed;
            double dy = Math.random() * 2 * speed - speed;
            double dz = Math.random() * 2 * speed - speed;

            double radius = 4;

            Vec3 leftAdjust = this.position().add(new Vec3(Math.sin(Math.toRadians(getYRot() + 90)), 0, Math.cos(Math.toRadians(getYRot() + 90))).scale(radius));
            Vec3 rightAdjust = this.position().add(new Vec3(Math.sin(Math.toRadians(getYRot() - 90)), 0, Math.cos(Math.toRadians(getYRot() - 90))).scale(radius));

//            // Left
//            level().addParticle(DTEParticleHelper.ESOTERIC_SPARKS, leftAdjust.x, leftAdjust.y, leftAdjust.z, dx, dy, dz);
//
//            // Right
//            level().addParticle(DTEParticleHelper.ESOTERIC_SPARKS, rightAdjust.x, rightAdjust.y, rightAdjust.z, dx, dy, dz);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        // Slightly slower so it doesn't travel as far before despawning
        return 1.2F;
    }

    @Override
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /*@Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        var target = pResult.getEntity();

        if (!entities.contains(target))
        {
            DamageSources.applyDamage(target, damage,
                    SpellRegistries.ESOTERIC_EDGE.get().getDamageSource(this, getOwner()));

            // Kills shields & Do effects
            if (target instanceof LivingEntity livingTarget)
            {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                if (livingTarget instanceof Player player)
                {
                    player.disableShield();
                }
                if (DamageSources.applyDamage(livingTarget, damage, SpellRegistries.ESOTERIC_EDGE.get().getDamageSource(this, getOwner())))
                {
                    EnchantmentHelper.doPostAttackEffects((ServerLevel) this.level(), livingTarget, SpellRegistries.ESOTERIC_EDGE.get().getDamageSource(this, getOwner()));
                }
            }
            if (target instanceof ShieldPart || target instanceof AbstractShieldEntity)
            {
                target.kill();
            }

            entities.add(target);
        }
    }*/

    private void damageEntity(Entity entity)
    {
        if (!entities.contains(entity))
        {
            DamageSources.applyDamage(entity, damage,
                    SpellRegistries.SLICE.get().getDamageSource(this, getOwner()));

            // Kills shields & Do effects
            if (entity instanceof LivingEntity livingTarget)
            {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 15, 0));
                if (livingTarget instanceof Player player)
                {
                    player.disableShield();
                }
                if (DamageSources.applyDamage(livingTarget, damage, SpellRegistries.SLICE.get().getDamageSource(this, getOwner())))
                {
                    EnchantmentHelper.doPostAttackEffects((ServerLevel) this.level(), livingTarget, SpellRegistries.SLICE.get().getDamageSource(this, getOwner()));
                }
            }
            if (entity instanceof ShieldPart || entity instanceof AbstractShieldEntity)
            {
                entity.kill();
            }

            entities.add(entity);
        }
    }

    /*@Override
    protected void onHit(HitResult hitresult) {
        super.onHit(hitresult);
        this.discard();
        pierceOrDiscard();
    }*/

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }



    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return pTarget != getOwner() && super.canHitEntity(pTarget);
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        //
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
}
