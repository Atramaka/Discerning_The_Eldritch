package net.feshy.cursed_sorcery.registries;

import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = CursedSorcery.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DTEAttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, CursedSorcery.MOD_ID);


    // Void
    public static final DeferredHolder<Attribute, Attribute> DARK_MAGIC_RESIST = registerResistanceAttribute("dark");
    public static final DeferredHolder<Attribute, Attribute> DARK_MAGIC_POWER = registerPowerAttribute("dark");


    // Void
    public static final DeferredHolder<Attribute, Attribute> CURSE_MAGIC_RESIST = registerResistanceAttribute("curse");
    public static final DeferredHolder<Attribute, Attribute> CURSE_MAGIC_POWER = registerPowerAttribute("curse");

    // Ritual
    public static final DeferredHolder<Attribute, Attribute> RITUAL_MAGIC_RESIST = registerResistanceAttribute("ritual");
    public static final DeferredHolder<Attribute, Attribute> RITUAL_MAGIC_POWER = registerPowerAttribute("ritual");

    public static void register(IEventBus eventBus)
    {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event)
    {
        event.getTypes().forEach(entityType ->
                ATTRIBUTES.getEntries().forEach(
                        attributeDeferredHolder -> event.add(entityType, attributeDeferredHolder
                        )));
    }

    // ;_;
    private static DeferredHolder<Attribute, Attribute> registerResistanceAttribute(String id)
    {
        return ATTRIBUTES.register(id + "_magic_resist", () ->
                (new MagicRangedAttribute("attribute.cursed_sorcery." + id + "_magic_resist",
                        1.0D, -100, 100).setSyncable(true)));
    }

    private static DeferredHolder<Attribute, Attribute> registerPowerAttribute(String id)
    {
        return ATTRIBUTES.register(id + "_spell_power", () ->
                (new MagicRangedAttribute("attribute.cursed_sorcery." + id + "_spell_power",
                        1.0D, -100, 100).setSyncable(true)));
    }

    private static DeferredHolder<Attribute, Attribute> registerAttribute(String id)
    {
        return ATTRIBUTES.register(id, () ->
                (new MagicRangedAttribute("attribute.aces_spell_utils." + id,
                        1.0D, -100, 100).setSyncable(true)));
    }
}
