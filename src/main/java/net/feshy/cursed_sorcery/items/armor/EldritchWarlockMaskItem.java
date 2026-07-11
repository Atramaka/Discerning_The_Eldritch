package net.feshy.cursed_sorcery.items.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

public class EldritchWarlockMaskItem extends ImbuableDTEArmorItem {
    public EldritchWarlockMaskItem(Type slot, Properties settings) {
        super(DTEArmorMaterialRegistry.ELDRITCH_WARLOCK, slot, settings, schoolAttributesWithResistance(AttributeRegistry.ELDRITCH_SPELL_POWER, AttributeRegistry.SUMMON_DAMAGE, 150, 0.15F, 0.05F, 0.05F));
    }
}
