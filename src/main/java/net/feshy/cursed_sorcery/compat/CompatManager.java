package net.feshy.cursed_sorcery.compat;

import net.neoforged.fml.ModList;

public class CompatManager {
    public static boolean isPastelLoaded()
    {
        return ModList.get().isLoaded("pastel");
    }
}
