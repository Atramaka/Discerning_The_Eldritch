package net.feshy.cursed_sorcery.items.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

public class EldritchWarlockArmorItem extends ImbuableDTEArmorItem {
    public EldritchWarlockArmorItem(Type slot, Properties settings) {
        super(DTEArmorMaterialRegistry.ELDRITCH_WARLOCK, slot, settings, schoolAttributesWithResistance(AttributeRegistry.ELDRITCH_SPELL_POWER, AttributeRegistry.MANA_REGEN, 150, 0.15F, 0.05F, 0.05F));
    }
}
