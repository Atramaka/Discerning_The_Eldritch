package net.feshy.cursed_sorcery.compat.pastel;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;

public class PastelCompatItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CursedSorcery.MOD_ID);

    // Dream Reaver Ymir
    public static final DeferredHolder<Item, Item> DREAM_REAVER_YMIR = ITEMS.register("dream_reaver_ymir", DreamReaverYmirItem::new);

    public static Collection<DeferredHolder<Item, ? extends Item>> getDTEItems()
    {
        return ITEMS.getEntries();
    }

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
