package net.feshy.cursed_sorcery.items.spellbooks;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.feshy.cursed_sorcery.registries.SpellRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class GuardianGuidebookSpellbook extends UniqueSpellBook {
    public GuardianGuidebookSpellbook() {
        super(SpellDataRegistryHolder.of(
                new SpellDataRegistryHolder(SpellRegistries.DISMANTLE, 1)
        ), 9);
        withSpellbookAttributes(
                new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE),
                new AttributeContainer(AttributeRegistry.EVOCATION_SPELL_POWER, 0.10F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        );
    }
}
