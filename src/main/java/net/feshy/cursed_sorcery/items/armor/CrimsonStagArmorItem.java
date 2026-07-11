package net.feshy.cursed_sorcery.items.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.feshy.cursed_sorcery.registries.DTEAttributeRegistry;

public class CrimsonStagArmorItem extends ImbuableDTEArmorItem {
    public CrimsonStagArmorItem(Type slot, Properties settings) {
        super(DTEArmorMaterialRegistry.CRIMSON_STAG, slot, settings,
                schoolAttributesWithResistance(
                        AttributeRegistry.BLOOD_SPELL_POWER,
                        DTEAttributeRegistry.RITUAL_MAGIC_POWER,
                        150,
                        0.15F,
                        0.05F,
                        0.05F
                ));
    }
}
