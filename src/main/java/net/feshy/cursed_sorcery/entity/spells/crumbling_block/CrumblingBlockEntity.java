package net.feshy.cursed_sorcery.entity.spells.crumbling_block;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Custom falling block that crumbles on impact instead of placing
 */
/**
 * Custom falling block that crumbles on impact instead of placing
 */
public class CrumblingBlockEntity extends FallingBlockEntity {

    private boolean hasCollided = false;
    public BlockState blockState = net.minecraft.world.level.block.Blocks.STONE.defaultBlockState();

    public CrumblingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
        this.dropItem = false;
    }

    @Override
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public void tick() {
        // Call parent tick
        super.tick();

        if (!this.level().isClientSide) {
            // Check if the block has landed (onGround will be true when it hits something)
            if (this.onGround() && !hasCollided) {
                hasCollided = true;
                crumbleAndDestroy();
                return;
            }

            // Also check for any collision with blocks
            if ((this.horizontalCollision || this.verticalCollision) && !hasCollided) {
                hasCollided = true;
                crumbleAndDestroy();
                return;
            }

            // Failsafe - crumble after 10 seconds if still falling
            if (this.time > 600) {
                crumbleAndDestroy();
                return;
            }
        }
    }

    private void crumbleAndDestroy() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            BlockState state = this.getBlockState();

            // Spawn dust/smoke particles
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x, pos.y, pos.z,
                    8, 0.4, 0.4, 0.4, 0.08);

            serverLevel.sendParticles(ParticleTypes.POOF,
                    pos.x, pos.y, pos.z,
                    12, 0.3, 0.3, 0.3, 0.1);

            // Spawn block-colored dust particles
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.x, pos.y, pos.z,
                    15, 0.4, 0.4, 0.4, 0.15);
        }

        // Remove the entity without placing a block
        this.discard();
    }
}