package net.feshy.cursed_sorcery.entity.spells.lapse_blue;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.feshy.cursed_sorcery.registries.DTEEntityRegistry;
import net.feshy.cursed_sorcery.registries.DTESoundRegistry;
import net.feshy.cursed_sorcery.registries.SpellRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;

public class LapseBlueEntity extends AoeEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Synced data for radius
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> DATA_CASTER_UUID = 
        SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // Duration in ticks (e.g., 5 seconds = 100 ticks)
    private int duration = 120;
    // Attraction strength multiplier
    private float attractionStrength = 0.4f;
    // Explosion damage multiplier
    private float explosionDamageMultiplier = 2.0f;
    // Explosion knockback strength
    private float explosionKnockback = 2f;
    // Spawn animation duration (ticks for the implosion effect)
    private static final int SPAWN_ANIM_DURATION = 15;
    private LivingEntity caster; // Reference to the player who cast the spell
    private int elapsedTicks = 0; // Track how many ticks have passed
    private static final float MIN_DISTANCE = 2.0f; // Start 2 blocks away
    private static final float MAX_DISTANCE = 10.0f; // End 10 blocks away
    private static final float FOLLOW_SPEED = 0.1f; // Smoothness factor (0.0-1.0, lower = smoother with more lag)

    private int soundTickCounter = 0;
    private static final int SOUND_INTERVAL = 20; // Play sound every 20 ticks (1 second)
    private static final int BLOCK_PULL_INTERVAL = 5; // Try to pull blocks every 10 ticks
    private static final double BLOCK_PULL_CHANCE = 0.03; // 30% chance per block to be pulled

    private double blockPullChance = 0.15;

    private static final double FALLING_BLOCK_DESTROY_CHANCE = 0.3; // 50% chance to destroy instead of explode
    private static final double CLOSE_CONTACT_DESTRUCTION_DISTANCE = 2; // Distance for destruction
    private static final int CLOSE_CONTACT_CHECK_INTERVAL = 2; // Check every 3 ticks for maximum destruction
    private static final double BLOCK_DESTRUCTION_CHANCE = 0.98; // 95% chance to destroy blocks in close proximity




    public void setBlockPullChance(double chance) {
        this.blockPullChance = chance;
    }

    private static final EntityDataAccessor<Integer> DATA_ELAPSED_TICKS =
    SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_MODEL_SCALE =
            SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_MIN_DISTANCE =
            SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_MAX_DISTANCE =
            SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DATA_DURATION =
            SynchedEntityData.defineId(LapseBlueEntity.class, EntityDataSerializers.INT);

    private boolean hasPlayedSpawnEffect = false;

    public LapseBlueEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public LapseBlueEntity(Level level, LivingEntity owner) {
        this(DTEEntityRegistry.LAPSE_BLUE.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RADIUS, 2.0f);
        builder.define(DATA_CASTER_UUID, java.util.Optional.empty());
        builder.define(DATA_ELAPSED_TICKS, 0);
        builder.define(DATA_MODEL_SCALE, 2.0f);
        builder.define(DATA_MIN_DISTANCE, 2.0f);
        builder.define(DATA_MAX_DISTANCE, 10.0f);
        builder.define(DATA_DURATION, 120);
    }


    private float minDistance = 2.0f;
    private float maxDistance = 10.0f;


    // Add these setters
    public void setDistances(float minDist, float maxDist) {
        this.minDistance = minDist;
        this.maxDistance = maxDist;
        this.entityData.set(DATA_MIN_DISTANCE, minDist);
        this.entityData.set(DATA_MAX_DISTANCE, maxDist);
    }

    public void setDuration(int ticks) {
        this.duration = ticks;
        this.entityData.set(DATA_DURATION, ticks);
    }

    public void setModelScale(float scale) {
        this.entityData.set(DATA_MODEL_SCALE, scale);
    }

    public float getModelScale() {
        return this.entityData.get(DATA_MODEL_SCALE);
    }




    
    public void setAttractionRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public float getAttractionRadius() {
        return this.entityData.get(DATA_RADIUS);
    }



    // Add a method to set the caster when creating the entity
    public void setCaster(LivingEntity caster) {
        this.caster = caster;
        if (caster != null) {
            // Sync the caster's UUID to clients
            this.entityData.set(DATA_CASTER_UUID, java.util.Optional.of(caster.getUUID()));
        }
    }

    public LivingEntity getCaster() {
        // First try the local reference (server-side)
        if (this.caster != null && this.caster.isAlive()) {
            return this.caster;
        }

        // Try to get from UUID (works on both client and server)
        var casterUUID = this.entityData.get(DATA_CASTER_UUID);
        if (casterUUID.isPresent() && this.level() != null) {
            // First try to find as a player
            Entity entity = this.level().getPlayerByUUID(casterUUID.get());
            if (entity instanceof LivingEntity livingEntity) {
                // Cache the reference for server-side
                if (!level().isClientSide()) {
                    this.caster = livingEntity;
                }
                return livingEntity;
            }

            // If not a player, search all entities (for non-player casters)
            // This is more expensive but necessary for mob casters
            if (!level().isClientSide() && level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                Entity foundEntity = serverLevel.getEntity(casterUUID.get());
                if (foundEntity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                    this.caster = livingEntity;
                    return livingEntity;
                }
            }
        }

        return null;
    }



    public int getDurationTicks() {
        return this.duration;
    }

    // Modify the tick() method to include smooth position updates


    // Helper method for linear interpolation
    private double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    public void setAttractionStrength(float strength) {
        this.attractionStrength = strength;
    }

    public void setExplosionDamageMultiplier(float multiplier) {
        this.explosionDamageMultiplier = multiplier;
    }

    public void setExplosionKnockback(float knockback) {
        this.explosionKnockback = knockback;
    }

    public int getElapsedTicks() {
        // Return synced data (works on both server and client)
        return this.entityData.get(DATA_ELAPSED_TICKS);
    }

    @Override
    public void tick() {
        super.tick();

        // Get caster using the method that works on both sides
        LivingEntity currentCaster = getCaster();

        if (!level().isClientSide()) {
            // Server-side logic
            if (currentCaster != null && currentCaster.isAlive()) {
                elapsedTicks++;
                this.entityData.set(DATA_ELAPSED_TICKS, elapsedTicks);

                // Calculate target position based on caster's look direction
                float progress = Math.min((float) elapsedTicks / (float) this.duration, 1.0f);
                float currentDistance = minDistance + (maxDistance - minDistance) * progress;

                Vec3 eyePos = currentCaster.getEyePosition();
                Vec3 lookDir = currentCaster.getLookAngle();
                Vec3 targetPos = eyePos.add(lookDir.scale(currentDistance));

                // Smoothly move toward target position using lerp
                Vec3 currentPos = this.position();
                double lerpFactor = 0.08; // How quickly to follow (0.0-1.0)
                Vec3 newPos = new Vec3(
                        lerp(currentPos.x, targetPos.x, lerpFactor),
                        lerp(currentPos.y, targetPos.y, lerpFactor),
                        lerp(currentPos.z, targetPos.z, lerpFactor)
                );

                // Directly set position on server
                this.setPos(newPos.x, newPos.y, newPos.z);
            }

            // Play passive sound effect
            soundTickCounter++;
            if (soundTickCounter >= SOUND_INTERVAL) {
                soundTickCounter = 0;
                Vec3 center = this.position();
                level().playSound(null, center.x, center.y, center.z,
                        DTESoundRegistry.LAPSE_BLUE_CAST.get(), SoundSource.AMBIENT, 0.5f, 0.8f + random.nextFloat() * 0.4f);
            }

            // Try to pull blocks periodically
            if (tickCount % BLOCK_PULL_INTERVAL == 0) {
                pullBlocks();
            }

            if (tickCount % CLOSE_CONTACT_CHECK_INTERVAL == 0) {
                destroyBlocksAndEntitiesInCloseContact();
            }

            // Server-side: Spawn implosion effect on first tick
            if (!hasPlayedSpawnEffect) {
                spawnImplosionEffect();
                spawnanimation((ServerLevel) level(), this.position(), getAttractionRadius());
                hasPlayedSpawnEffect = true;
            }

            // Server-side: Apply attraction to nearby entities
            pullNearbyEntities();

            // Apply damage to entities very close to the center
            if (tickCount % 10 == 0) {
                checkHits();
            }

            // Explode and discard when duration expires
            if (tickCount >= duration) {
                explode();
                discard();
            }
        } else {
            // Client-side: Smoothly interpolate position towards where it should be based on caster's look
            if (currentCaster != null && currentCaster.isAlive()) {
                int syncedElapsedTicks = getElapsedTicks();
                int syncedDuration = this.entityData.get(DATA_DURATION);
                float syncedMinDistance = this.entityData.get(DATA_MIN_DISTANCE);
                float syncedMaxDistance = this.entityData.get(DATA_MAX_DISTANCE);

                float progress = Math.min((float) syncedElapsedTicks / (float) syncedDuration, 1.0f);
                float currentDistance = syncedMinDistance + (syncedMaxDistance - syncedMinDistance) * progress;

                Vec3 eyePos = currentCaster.getEyePosition();
                Vec3 lookDir = currentCaster.getLookAngle();
                Vec3 targetPos = eyePos.add(lookDir.scale(currentDistance));

                // Optional: Add slight smoothing for visual polish (0.5-0.8 is good)
                Vec3 currentPos = this.position();
                double lerpFactor = 0.08; // Higher = snappier, lower = smoother but laggier
                Vec3 newPos = new Vec3(
                        lerp(currentPos.x, targetPos.x, lerpFactor),
                        lerp(currentPos.y, targetPos.y, lerpFactor),
                        lerp(currentPos.z, targetPos.z, lerpFactor)
                );
                this.setPos(newPos.x, newPos.y, newPos.z);
            }

            // Client-side: Play spawn animation particles during early ticks
            if (tickCount <= SPAWN_ANIM_DURATION) {
                spawnImplosionParticlesClient();
            }

            // Client-side: Spawn particles for visual effect
            spawnSphericalParticles();
        }
    }

    /**
     * Destroys both solid blocks and falling block entities that come into very close contact with the vortex
     * This is the main destruction mechanism - high probability of complete annihilation
     */
    private void destroyBlocksAndEntitiesInCloseContact() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        // Check if terrain destruction is enabled
        if (!net.feshy.cursed_sorcery.utils.DTEServerConfig.enableTerrainDestruction) {
            return;
        }

        Vec3 center = this.position();

        // Get all entities and blocks within close contact distance
        AABB destructionArea = new AABB(
                center.subtract(CLOSE_CONTACT_DESTRUCTION_DISTANCE, CLOSE_CONTACT_DESTRUCTION_DISTANCE, CLOSE_CONTACT_DESTRUCTION_DISTANCE),
                center.add(CLOSE_CONTACT_DESTRUCTION_DISTANCE, CLOSE_CONTACT_DESTRUCTION_DISTANCE, CLOSE_CONTACT_DESTRUCTION_DISTANCE)
        );

        // Destroy falling block entities in close contact
        List<Entity> fallingBlocks = level().getEntities(this, destructionArea, entity ->
                entity instanceof net.minecraft.world.entity.item.FallingBlockEntity &&
                        entity.distanceTo(this) <= CLOSE_CONTACT_DESTRUCTION_DISTANCE
        );

        for (Entity entity : fallingBlocks) {
            if (entity instanceof net.minecraft.world.entity.item.FallingBlockEntity fallingBlock) {
                if (random.nextDouble() < BLOCK_DESTRUCTION_CHANCE) {
                    spawnBlockDestructionEffect(serverLevel, fallingBlock.position());
                    fallingBlock.discard();
                }
            }
        }

        // Destroy actual blocks in close contact using same hardness rules as pullBlocks()
        BlockPos centerPos = BlockPos.containing(center);
        int destroyRadius = (int) Math.ceil(CLOSE_CONTACT_DESTRUCTION_DISTANCE);

        for (int x = -destroyRadius; x <= destroyRadius; x++) {
            for (int y = -destroyRadius; y <= destroyRadius; y++) {
                for (int z = -destroyRadius; z <= destroyRadius; z++) {
                    BlockPos blockPos = centerPos.offset(x, y, z);
                    double distToBlock = center.distanceTo(blockPos.getCenter());

                    // Only destroy blocks within the close contact radius
                    if (distToBlock > CLOSE_CONTACT_DESTRUCTION_DISTANCE) continue;

                    net.minecraft.world.level.block.state.BlockState blockState = level().getBlockState(blockPos);

                    // Skip air blocks
                    if (blockState.isAir()) continue;

                    // Get block hardness - same as pullBlocks() method
                    float blockHardness = blockState.getDestroySpeed(level(), blockPos);

                    // Skip unbreakable blocks (bedrock, obsidian, etc) - same threshold as pullBlocks()
                    if (blockHardness < 0 || blockHardness >= 3.5f) continue;

                    // High chance to destroy breakable blocks in close contact
                    if (random.nextDouble() < BLOCK_DESTRUCTION_CHANCE) {
                        // Destroy the block without drops
                        level().destroyBlock(blockPos, false);

                        // Spawn destruction effect
                        spawnBlockDestructionEffect(serverLevel, blockPos.getCenter());
                    }
                }
            }
        }
    }

    /**
     * Spawns a poof/smoke particle effect to represent block destruction and turning to dust
     */
    private void spawnBlockDestructionEffect(ServerLevel serverLevel, Vec3 position) {
        // Main poof cloud (larger)
        serverLevel.sendParticles(
                ParticleTypes.POOF,
                position.x, position.y, position.z,
                12, 0.4, 0.4, 0.4, 0.12
        );

        // Smoke effect for a more ethereal destruction
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                position.x, position.y, position.z,
                20, 0.3, 0.3, 0.3, 0.08
        );

        // Add some ash particles for the "turning to dust" effect
        serverLevel.sendParticles(
                ParticleTypes.ASH,
                position.x, position.y, position.z,
                6, 0.25, 0.25, 0.25, 0.06
        );

        // Optional: Add some warped spore particles for an eldritch feel
        serverLevel.sendParticles(
                ParticleTypes.WARPED_SPORE,
                position.x, position.y, position.z,
                4, 0.2, 0.2, 0.2, 1.5f
        );
    }

    private void pullBlocks() {
        if (!(level() instanceof ServerLevel serverLevel)) return;


        // Check if terrain destruction is enabled
        if (!net.feshy.cursed_sorcery.utils.DTEServerConfig.enableTerrainDestruction) {
            return;
        }

        float radius = getAttractionRadius();
        Vec3 center = this.position();
        BlockPos centerPos = BlockPos.containing(center);

        // Search in a cube around the vortex
        int searchRadius = (int) Math.ceil(radius);

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos blockPos = centerPos.offset(x, y, z);
                    double distToBlock = center.distanceTo(blockPos.getCenter());

                    // Only check blocks within radius
                    if (distToBlock > radius) continue;

                    net.minecraft.world.level.block.state.BlockState blockState = level().getBlockState(blockPos);

                    // Skip air blocks
                    if (blockState.isAir()) continue;

                    // Get block hardness
                    float blockHardness = blockState.getDestroySpeed(level(), blockPos);

                    // Skip unbreakable and very hard blocks
                    if (blockHardness < 0 || blockHardness >= 3.5f) continue;

                    // Random chance to pull this block (now scales with spell power)
                    if (random.nextDouble() > blockPullChance) continue;

                    // Break the block without drops
                    level().destroyBlock(blockPos, false);

                    // Create a falling block entity
                    net.minecraft.world.entity.item.FallingBlockEntity fallingBlock =
                            net.minecraft.world.entity.item.FallingBlockEntity.fall(level(), blockPos, blockState);

                    fallingBlock.setHurtsEntities(0f, 0);
                    fallingBlock.time = 1;
                    fallingBlock.dropItem = false;

                    level().addFreshEntity(fallingBlock);

                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.POOF,
                            blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                            3, 0.3, 0.3, 0.3, 0.1
                    );
                }
            }
        }
    }

    /**
     * Server-side: Sends implosion particles to all clients
     */
    private void spawnImplosionEffect() {
        if (level() instanceof ServerLevel serverLevel) {
            float radius = getAttractionRadius();
            Vec3 center = this.position();
            
            // Create a burst of particles rushing inward from a sphere
            int particleCount = 60;
            
            for (int i = 0; i < particleCount; i++) {
                // Spherical distribution
                double phi = Math.acos(1 - 2 * random.nextDouble());
                double theta = random.nextDouble() * Math.PI * 2;
                
                // Start particles at the outer edge
                double startRadius = radius * 4;
                double startX = center.x + startRadius * Math.sin(phi) * Math.cos(theta);
                double startY = center.y + startRadius * Math.cos(phi);
                double startZ = center.z + startRadius * Math.sin(phi) * Math.sin(theta);
                
                // Velocity pointing toward center (negative of outward direction)
                double speed = 0.15 + random.nextDouble() * 0.1;
                double vx = -speed * Math.sin(phi) * Math.cos(theta);
                double vy = -speed * Math.cos(phi);
                double vz = -speed * Math.sin(phi) * Math.sin(theta);
                
                // Soul fire flame particles rushing inward
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        startX, startY, startZ,
                        0,
                        vx, vy, vz,
                        1.0
                );
            }
            
            // Add some electric sparks for extra flair
            for (int i = 0; i < 20; i++) {
                double phi = Math.acos(1 - 2 * random.nextDouble());
                double theta = random.nextDouble() * Math.PI * 2;
                
                double startRadius = radius * 2;
                double startX = center.x + startRadius * Math.sin(phi) * Math.cos(theta);
                double startY = center.y + startRadius * Math.cos(phi);
                double startZ = center.z + startRadius * Math.sin(phi) * Math.sin(theta);
                
                double speed = 0.2;
                double vx = -speed * Math.sin(phi) * Math.cos(theta);
                double vy = -speed * Math.cos(phi);
                double vz = -speed * Math.sin(phi) * Math.sin(theta);
                
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        startX, startY, startZ,
                        3,
                        vx, vy, vz,
                        0.05
                );
            }
            
            // Reverse sonic boom / warped particles at center for that "reality tear" feel
//            serverLevel.sendParticles(
//                    ParticleTypes.SOUL,
//                    center.x, center.y, center.z,
//                    15,
//                    0.3, 0.3, 0.3,
//                    0.05
//            );
        }
    }

    /**
     * Client-side: Continuous implosion particles during spawn animation
     */
    private void spawnImplosionParticlesClient() {
        float radius = getAttractionRadius();
        float progress = (float) tickCount / SPAWN_ANIM_DURATION;
        
        // More particles at the start, fewer as it completes
        int particleCount = (int) ((1.0f - progress) * 12) + 2;
        
        // Particles start far and get closer as animation progresses
        float currentRadius = radius * (1.5f - progress * 0.5f);




        for (int i = 0; i < particleCount; i++) {
            double phi = Math.acos(1 - 2 * random.nextDouble());
            double theta = random.nextDouble() * Math.PI * 2;
            
            double x = getX() + currentRadius * Math.sin(phi) * Math.cos(theta);
            double y = getY() + currentRadius * Math.cos(phi);
            double z = getZ() + currentRadius * Math.sin(phi) * Math.sin(theta);
            
            // Velocity toward center, faster as animation progresses
            double speed = 0.1 + progress * 0.15;
            Vec3 toCenter = this.position().subtract(x, y, z).normalize().scale(speed);
            
            level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    toCenter.x, toCenter.y, toCenter.z
            );


        }

        // Swirling ring that contracts
        double ringRadius = currentRadius * 0.8;
        for (int i = 0; i < 4; i++) {
            double angle = (tickCount * 0.3 + i * (Math.PI / 2)) % (Math.PI * 2);
            double x = getX() + Math.cos(angle) * ringRadius;
            double y = getY();
            double z = getZ() + Math.sin(angle) * ringRadius;
            
            Vec3 toCenter = this.position().subtract(x, y, z).normalize().scale(0.1);


            level().playSound(null, toCenter.x, toCenter.y, toCenter.z,
                    DTESoundRegistry.LAPSE_BLUE_SUMMON.get(), SoundSource.AMBIENT, 2.0f, 1f);



            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    x, y, z,
                    toCenter.x, 0.02, toCenter.z
            );
        }
    }


    private void spawnanimation(ServerLevel serverLevel, Vec3 center, float radius) {
        int burstCount = 50;

        for (int i = 0; i < burstCount; i++) {
            // Spherical burst outward
            double phi = Math.acos(1 - 2 * random.nextDouble());
            double theta = random.nextDouble() * Math.PI * 2;

            double speed = 0.3 + random.nextDouble() * 0.4;

            double vx = speed * Math.sin(phi) * Math.cos(theta);
            double vy = speed * Math.cos(phi);
            double vz = speed * Math.sin(phi) * Math.sin(theta);

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    center.x, center.y, center.z,
                    5,
                    vx, vy, vz,
                    0.1
            );
        }
        serverLevel.playSound(null, center.x, center.y, center.z,
                DTESoundRegistry.LAPSE_BLUE_EXPLODE.get(), SoundSource.AMBIENT, 2.0f, 0.9f);
        // Add some flash particles at center
        serverLevel.sendParticles(
                ParticleTypes.FLASH,
                center.x, center.y, center.z,
                1,
                0, 0, 0,
                0
        );

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                center.x, center.y, center.z,
                3,
                0, 0, 0,
                0
        );
    }


    private void pullNearbyEntities() {
        float radius = getAttractionRadius();
        Vec3 center = this.position();

        AABB pullArea = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        List<Entity> entities = level().getEntities(this, pullArea, entity ->
                entity != getOwner() &&
                        (entity instanceof LivingEntity ||
                                entity instanceof net.minecraft.world.entity.item.ItemEntity ||
                                entity instanceof net.minecraft.world.entity.item.FallingBlockEntity) &&
                        entity.distanceTo(this) <= radius
        );

        for (Entity entity : entities) {
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vec3 direction = center.subtract(entityPos);
            double distance = direction.length();

            if (distance > 0.5) {
                double pullStrength = attractionStrength * (1 - (distance / radius));
                Vec3 pullVector = direction.normalize().scale(pullStrength);

                entity.setDeltaMovement(entity.getDeltaMovement().add(pullVector));
                entity.hurtMarked = true;

                if (entity instanceof net.minecraft.world.entity.item.FallingBlockEntity fallingBlock) {
                    fallingBlock.time = 1; // Reset time to keep it suspended
                    fallingBlock.dropItem = false; // Prevent settling
                }
            }
        }
    }




    /**
     * Spawns particles in a proper spherical pattern around the entity
     */
    private void spawnSphericalParticles() {
        float radius = getAttractionRadius();
        int particleCount = 8; // Particles per tick

        for (int i = 0; i < particleCount; i++) {
            // Use spherical coordinates for even distribution
            // Phi: angle from vertical axis (0 to PI)
            // Theta: angle around vertical axis (0 to 2*PI)
            double phi = Math.acos(1 - 2 * random.nextDouble()); // Uniform distribution on sphere
            double theta = random.nextDouble() * Math.PI * 2;

            // Random radius between 30% and 100% of max radius
            double r = radius * (0.3 + random.nextDouble() * 0.7);

            // Convert spherical to cartesian coordinates
            double x = getX() + r * Math.sin(phi) * Math.cos(theta);
            double y = getY() + r * Math.cos(phi);
            double z = getZ() + r * Math.sin(phi) * Math.sin(theta);

            // Velocity pointing toward center
            Vec3 toCenter = this.position().subtract(x, y, z).normalize().scale(0.15);

//            level().addParticle(
//                    ParticleTypes.SOUL_FIRE_FLAME,
//                    x, y, z,
//                    toCenter.x, toCenter.y, toCenter.z
//            );
        }

        // Add some swirling particles closer to center for visual interest
        for (int i = 0; i < 3; i++) {
            double swirl = (tickCount * 0.15 + i * (Math.PI * 2 / 3)) % (Math.PI * 2);
            double innerR = radius * 0.2;

            double x = getX() + Math.cos(swirl) * innerR;
            double y = getY() + Math.sin(tickCount * 0.1) * 0.3;
            double z = getZ() + Math.sin(swirl) * innerR;

            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    x, y, z,
                    0, 0.02, 0
            );
        }
    }

    /**
     * Called when the entity expires - deals damage and knockback to all entities in range
     */
    private void explode() {
        float radius = getAttractionRadius();
        Vec3 center = this.position();

        // Play explosion sound
        level().playSound(null, center.x, center.y, center.z,
                DTESoundRegistry.LAPSE_BLUE_EXPLODE.get(), SoundSource.AMBIENT, 2.0f, 0.9f);

        // Get all entities in explosion radius
        AABB explosionArea = new AABB(
                center.subtract(radius, radius, radius),
                center.add(radius, radius, radius)
        );

        List<Entity> entities = level().getEntities(this, explosionArea, entity ->
                entity != getOwner() &&
                        entity instanceof LivingEntity &&
                        entity.distanceTo(this) <= radius
        );

        float explosionDamage = getDamage() * explosionDamageMultiplier;

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget) {
                double distance = entity.distanceTo(this);

                // Damage scales inversely with distance (closer = more damage)
                float damageMultiplier = (float) (1.0 - (distance / radius) * 0.5);
                float finalDamage = explosionDamage * damageMultiplier;

                // Apply damage
                DamageSources.applyDamage(livingTarget, finalDamage,
                        SpellRegistries.LAPSE_BLUE.get().getDamageSource(this, getOwner()));

                // Calculate knockback direction (away from center)
                Vec3 knockbackDir = entity.position().subtract(center).normalize();
                if (knockbackDir.length() < 0.1) {
                    knockbackDir = new Vec3(random.nextDouble() - 0.5, 1, random.nextDouble() - 0.5).normalize();
                }

                // Knockback strength also scales with proximity
                float knockbackMultiplier = (float) (1.0 - (distance / radius) * 0.3);
                Vec3 knockback = knockbackDir.scale(explosionKnockback * knockbackMultiplier);

                // Apply knockback
                livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(knockback.x, knockback.y + 0.3, knockback.z));
                livingTarget.hurtMarked = true;
            }
        }

        // Handle falling blocks - destroy some, let others fall
        List<Entity> fallingBlocks = level().getEntities(this, explosionArea, entity ->
                entity instanceof net.minecraft.world.entity.item.FallingBlockEntity &&
                        entity.distanceTo(this) <= radius
        );

        for (Entity entity : fallingBlocks) {
            if (entity instanceof net.minecraft.world.entity.item.FallingBlockEntity fallingBlock) {
                // Random chance to destroy the falling block
                if (random.nextDouble() < FALLING_BLOCK_DESTROY_CHANCE) {
                    fallingBlock.discard(); // Destroy it
                } else {
                    // Let it fall naturally - reset time and dropItem so it settles
                    fallingBlock.time = 0;
                    fallingBlock.dropItem = true;
                }
            }
        }

        // Spawn explosion particles (server-side, sent to clients)
        if (level() instanceof ServerLevel serverLevel) {
            spawnExplosionParticles(serverLevel, center, radius);
        }
    }

    /**
     * Spawns a burst of particles when the entity explodes
     */
    private void spawnExplosionParticles(ServerLevel serverLevel, Vec3 center, float radius) {
        int burstCount = 50;

        for (int i = 0; i < burstCount; i++) {
            // Spherical burst outward
            double phi = Math.acos(1 - 2 * random.nextDouble());
            double theta = random.nextDouble() * Math.PI * 2;

            double speed = 0.3 + random.nextDouble() * 0.4;

            double vx = speed * Math.sin(phi) * Math.cos(theta);
            double vy = speed * Math.cos(phi);
            double vz = speed * Math.sin(phi) * Math.sin(theta);

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    center.x, center.y, center.z,
                    5,
                    vx, vy, vz,
                    0.1
            );
        }

        // Add some flash particles at center
        serverLevel.sendParticles(
                ParticleTypes.FLASH,
                center.x, center.y, center.z,
                1,
                0, 0, 0,
                0
        );

        serverLevel.sendParticles(
                ParticleTypes.SONIC_BOOM,
                center.x, center.y, center.z,
                1,
                0, 0, 0,
                0
        );
    }

    @Override
    public void applyEffect(LivingEntity target) {
        if (target != getOwner()) {
            double dist = target.distanceTo(this);
            if (dist < 1.5) {
                DamageSources.applyDamage(target, getDamage(),
                        SpellRegistries.LAPSE_BLUE.get().getDamageSource(this, getOwner()));
            }
        }
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(1.5, 1.5, 1.5);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    // ==================== GECKOLIB ANIMATION ====================

    // Make sure this matches EXACTLY the animation name in your .animation.json file
    // Check your bluetest.animation.json for the exact name (e.g., "animation.bluetest.idle")
    private final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.blue1.new");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            state.getController().setAnimation(IDLE_ANIM);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        // On client side, if we have a caster, ignore server position updates
        // and let our tick() method handle smooth positioning
        if (level().isClientSide() && getCaster() != null) {
            // Don't call super - we handle positioning ourselves
            return;
        }
        super.lerpTo(x, y, z, yRot, xRot, steps);
    }

}
