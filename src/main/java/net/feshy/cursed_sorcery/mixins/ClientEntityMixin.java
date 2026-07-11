package net.feshy.cursed_sorcery.mixins;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)

public class ClientEntityMixin {

    /**
     * Necessary see color glowing mob outlines while we have the echolocation effect
     */
    @Inject(method = "getTeamColor", at = @At(value = "HEAD"), cancellable = true)
    public void changeGlowOutline(CallbackInfoReturnable<Integer> cir) {
        if (ClientMagicData.getActiveSummons().contains(((Entity) (Object) this).getUUID())) {
            cir.setReturnValue(ClientConfigs.summonGlowColor);
        } else if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT)) {
            cir.setReturnValue(0x94F8FF);
        }
    }
}
