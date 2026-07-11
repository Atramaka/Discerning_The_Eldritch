package net.feshy.cursed_sorcery.compat;

import net.feshy.cursed_sorcery.compat.pastel.PastelCompatItems;
import net.neoforged.bus.api.IEventBus;

public class CompatRegistry {
    public static void registerPastelItems(IEventBus modBus)
    {
        if (CompatManager.isPastelLoaded())
        {
            PastelCompatItems.register(modBus);
        }
    }
}
