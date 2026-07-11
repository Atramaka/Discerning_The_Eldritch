package net.feshy.cursed_sorcery.mixins;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    /**
     * Necessary make entities appear glowing on our client while we have the echolocation effect
     */
    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void irons_spellbooks$changeGlowOutline(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (ClientConfigs.SUMMONS_GLOW.get() && ClientMagicData.getActiveSummons().contains(pEntity.getUUID())) {
            cir.setReturnValue(true);
        } else if (Minecraft.getInstance().player.hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT) && pEntity instanceof LivingEntity && Mth.abs((float) (pEntity.getY() - Minecraft.getInstance().player.getY())) < 18) {
            cir.setReturnValue(true);
        }
    }
}
