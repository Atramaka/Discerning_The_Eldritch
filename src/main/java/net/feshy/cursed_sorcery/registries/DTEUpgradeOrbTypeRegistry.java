package net.feshy.cursed_sorcery.registries;

import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.resources.ResourceKey;

public class DTEUpgradeOrbTypeRegistry {

    public static ResourceKey<UpgradeOrbType> ELDRITCH_SPELL_POWER = ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, CursedSorcery.id("eldritch_power"));

    public static ResourceKey<UpgradeOrbType> RITUAL_SPELL_POWER = ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, CursedSorcery.id("ritual_power"));
}
