package net.feshy.cursed_sorcery.entity.spells.lapse_blue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LapseBlueRenderer extends GeoEntityRenderer<LapseBlueEntity> {

    private static final float MODEL_SCALE = 2f;

    public LapseBlueRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LapseBlueModel());
    }

    @Override
    public void preRender(PoseStack poseStack, LapseBlueEntity animatable, BakedGeoModel model,
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
    public void actuallyRender(PoseStack poseStack, LapseBlueEntity animatable, BakedGeoModel model,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {

        // Use full brightness to make it glow
        int fullBright = 0xF000F0;

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer,
                isReRender, partialTick, fullBright, packedOverlay, colour);
    }

    @Override
    public RenderType getRenderType(LapseBlueEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentEmissive(texture);
    }

    @Override
    protected float getDeathMaxRotation(LapseBlueEntity entityLivingBaseIn) {
        return 0;
    }

    @Override
    public boolean shouldShowName(LapseBlueEntity animatable) {
        return false;
    }
}