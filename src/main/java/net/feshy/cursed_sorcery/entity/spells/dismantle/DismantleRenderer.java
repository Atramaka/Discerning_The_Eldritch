package net.feshy.cursed_sorcery.entity.spells.dismantle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class DismantleRenderer extends EntityRenderer<Dismantle> {
    private static final ResourceLocation[] TEXTURES = {
            CursedSorcery.id("textures/entity/dismantle/dismantle_1.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_2.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_3.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_4.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_5.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_6.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_7.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_8.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_9.png"),
            CursedSorcery.id("textures/entity/dismantle/dismantle_10.png")
    };

    // The "slash direction" is a roll around the projectile's forward axis.
    // These are the 3/4 iconic cut directions you described: top-down, left->right, diagonals.
    private static final float[] SLASH_ROLL_DEGREES = new float[]{
            0f,     // top-down (vertical)
            90f,    // left-to-right (horizontal)
            -90f,
            45f,    // diagonal \
            15f,    // diagonal \
            -15f,    // diagonal \
            -45f    // diagonal /
    };

    public DismantleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Dismantle dismantle, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        PoseStack.Pose pose = poseStack.last();

        // Billboard to the camera so the animated texture is always visible to the player
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Keep the "slash direction" as a roll on the billboarded quad
        float roll = getSlashRollDegrees(dismantle);
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

        createSlashTexturePlace(pose, dismantle, bufferSource, dismantle.getBbWidth() * 1.5F);

        poseStack.popPose();
        super.render(dismantle, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private float getSlashRollDegrees(Dismantle dismantle) {
        // Deterministic per projectile so the barrage cycles cleanly.
        // Using entity id means each spawned slash gets the next "cut direction".
        int index = Math.floorMod(dismantle.getId(), SLASH_ROLL_DEGREES.length);
        return SLASH_ROLL_DEGREES[index];
    }

    private void createSlashTexturePlace(PoseStack.Pose pose, Dismantle dismantle, MultiBufferSource bufferSource, float width) {
        Matrix4f poseMatrix = pose.pose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(dismantle)));

        float halfWidth = width * 0.2F;
        float height = dismantle.getBbHeight() * 0.5F;

        consumer.addVertex(poseMatrix, -halfWidth, height, -halfWidth).setColor(255, 255, 255, 255).setUv(0F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
        consumer.addVertex(poseMatrix, halfWidth, height, -halfWidth).setColor(255, 255, 255, 255).setUv(1F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
        consumer.addVertex(poseMatrix, halfWidth, height, halfWidth).setColor(255, 255, 255, 255).setUv(1F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
        consumer.addVertex(poseMatrix, -halfWidth, height, halfWidth).setColor(255, 255, 255, 255).setUv(0F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
    }

    @Override
    public ResourceLocation getTextureLocation(Dismantle dismantle) {
        int frame = (dismantle.tickCount / 1) % TEXTURES.length;
        return TEXTURES[frame];
    }
}
