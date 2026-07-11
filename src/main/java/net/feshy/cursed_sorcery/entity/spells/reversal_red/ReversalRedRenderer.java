package net.feshy.cursed_sorcery.entity.spells.reversal_red;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ReversalRedRenderer extends GeoEntityRenderer<ReversalRedEntity> {

    private static final float MODEL_SCALE = 2f;

    public ReversalRedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ReversalRedModel());
    }


    @Override
    public void preRender(PoseStack poseStack, ReversalRedEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          int colour) {
        // Scale the model based on entity's scale
        float scale = animatable.getModelScale();
        poseStack.scale(scale, scale, scale);

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, ReversalRedEntity animatable, BakedGeoModel model,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {

        // Use full brightness to make it glow
        int fullBright = 0xF000F0;

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer,
                isReRender, partialTick, fullBright, packedOverlay, colour);
    }

    @Override
    public RenderType getRenderType(ReversalRedEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentEmissive(texture);
    }

    @Override
    protected float getDeathMaxRotation(ReversalRedEntity entityLivingBaseIn) {
        return 0;
    }

    @Override
    public boolean shouldShowName(ReversalRedEntity animatable) {
        return false;
    }
}