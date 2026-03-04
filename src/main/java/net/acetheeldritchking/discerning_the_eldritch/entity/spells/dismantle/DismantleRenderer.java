package net.acetheeldritchking.discerning_the_eldritch.entity.spells.dismantle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Random;

public class DismantleRenderer extends EntityRenderer<Dismantle> {
    private static final ResourceLocation[] TEXTURES = {
            DiscerningTheEldritch.id("textures/entity/dismantle/dismantle_1.png"),
            DiscerningTheEldritch.id("textures/entity/dismantle/dismantle_2.png"),
            DiscerningTheEldritch.id("textures/entity/dismantle/dismantle_3.png"),
            DiscerningTheEldritch.id("textures/entity/dismantle/dismantle_4.png")
    };

    public DismantleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Dismantle dismantle, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        PoseStack.Pose pose = poseStack.last();

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, dismantle.yRotO, dismantle.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTick, dismantle.xRotO, dismantle.getXRot())));
        float randomZ = new Random(31L * dismantle.getId()).nextInt(-8, 8);
        poseStack.mulPose(Axis.XP.rotationDegrees(randomZ));

        createSlashTexturePlace(pose, dismantle, bufferSource, dismantle.getBbWidth() * 1.5F);

        poseStack.popPose();

        super.render(dismantle, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void createSlashTexturePlace(PoseStack.Pose pose, Dismantle dismantle, MultiBufferSource bufferSource, float width)
    {
        Matrix4f poseMatrix = pose.pose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(dismantle)));

        float halfWidth = width * 0.5F;
        float height = dismantle.getBbHeight() * 0.5F;

        consumer.addVertex(poseMatrix, -halfWidth, height, -halfWidth).setColor(255, 255, 255, 255).setUv(0F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
        consumer.addVertex(poseMatrix, halfWidth, height, -halfWidth).setColor(255, 255, 255, 255).setUv(1F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
        consumer.addVertex(poseMatrix, halfWidth, height, halfWidth).setColor(255, 255, 255, 255).setUv(1F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
        consumer.addVertex(poseMatrix, -halfWidth, height, halfWidth).setColor(255, 255, 255, 255).setUv(0F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0F, 1F, 0F);
    }

    @Override
    public ResourceLocation getTextureLocation(Dismantle dismantle) {
        int frame = (dismantle.tickCount / 4) % TEXTURES.length;
        return TEXTURES[frame];
    }
}
