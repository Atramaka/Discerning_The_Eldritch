package net.feshy.cursed_sorcery.items.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.curios.SimpleDescriptiveCurio;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.acetheeldritchking.aces_spell_utils.registries.ASAttributeRegistry;
import net.feshy.cursed_sorcery.items.custom.DTEItemDispatcher;
import net.feshy.cursed_sorcery.registries.DTEAttributeRegistry;
import net.feshy.cursed_sorcery.registries.DTEPotionEffectRegistry;
import net.feshy.cursed_sorcery.utils.DTERarities;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;


public class SixEyesCurio extends SimpleDescriptiveCurio {
    public final DTEItemDispatcher dispatcher;

    public SixEyesCurio() {
        super(ItemPropertiesHelper.equipment().stacksTo(1).fireResistant().rarity(DTERarities.APOTHIC_RARITY_PROXY.getValue()), Curios.NECKLACE_SLOT);
        this.dispatcher = new DTEItemDispatcher();
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> attr = LinkedHashMultimap.create();
        attr.put(DTEAttributeRegistry.CURSE_MAGIC_POWER, new AttributeModifier(id, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        attr.put(AttributeRegistry.MANA_REGEN, new AttributeModifier(id, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        attr.put(AttributeRegistry.MAX_MANA, new AttributeModifier(id, 200, AttributeModifier.Operation.ADD_VALUE));
//        attr.put(ASAttributeRegistry.MANA_STEAL, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
//        attr.put(ASAttributeRegistry.MANA_REND, new AttributeModifier(id, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        return attr;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
    
        // Apply Six Eyes effect while equipped - use very long duration to prevent flickering
        if (!slotContext.entity().hasEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT)) {
            slotContext.entity().addEffect(new MobEffectInstance(
                DTEPotionEffectRegistry.SIX_EYES_EFFECT,
                20,  // 50 seconds - long enough to prevent gaps between applications
                0,
                false,
                false,
                true
            ));
        } else {
            // Refresh the effect to keep it active indefinitely while equipped
            MobEffectInstance existingEffect = slotContext.entity().getEffect(DTEPotionEffectRegistry.SIX_EYES_EFFECT);
            if (existingEffect != null && existingEffect.getDuration() < 100) {
                // Refresh when duration gets low
                slotContext.entity().addEffect(new MobEffectInstance(
                    DTEPotionEffectRegistry.SIX_EYES_EFFECT,
                    20,
                    0,
                    false,
                    false,
                    true
                ));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player )
        {
            dispatcher.idle(player, stack);
        }
    }

}
