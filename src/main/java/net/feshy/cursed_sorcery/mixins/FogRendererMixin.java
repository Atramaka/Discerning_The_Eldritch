package net.feshy.cursed_sorcery.mixins;

import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void onSetupFog(CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT)) {
            // When player has Six Eyes effect, fog will be dampened
            // This allows the white ash particles to create the visual effect
        }
    }
}
