package net.feshy.cursed_sorcery.registries;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.utils.DTETags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.api.registry.SchoolRegistry.SCHOOL_REGISTRY_KEY;

public class DTESchoolRegistry {
    private static final DeferredRegister<SchoolType> DTE_SCHOOLS = DeferredRegister.create(SCHOOL_REGISTRY_KEY, CursedSorcery.MOD_ID);

    public static void register(IEventBus eventBus)
    {
        DTE_SCHOOLS.register(eventBus);
    }

    private static Supplier<SchoolType> registerSchool(SchoolType type)
    {
        return DTE_SCHOOLS.register(type.getId().getPath(), () -> type);
    }



    public static final ResourceLocation CURSE_RESOURCE = CursedSorcery.id("curse");

    public static final Supplier<SchoolType> CURSE = registerSchool(new SchoolType
            (
                    CURSE_RESOURCE,
                    DTETags.CURSE_FOCUS,
                    Component.translatable("school.cursed_sorcery.curse").withStyle(Style.EMPTY.withColor(0x870b32)),
                    DTEAttributeRegistry.CURSE_MAGIC_POWER,
                    DTEAttributeRegistry.CURSE_MAGIC_RESIST,
                    SoundRegistry.TELEKINESIS_CAST,
                    DTEDamageTypes.CURSE_MAGIC
            ));

    public static final ResourceLocation RITUAL_RESOURCE = CursedSorcery.id("ritual");

    public static final Supplier<SchoolType> RITUAL = registerSchool(new SchoolType
            (
                    RITUAL_RESOURCE,
                    DTETags.RITUAL_FOCUS,
                    Component.translatable("school.cursed_sorcery.ritual").withStyle(Style.EMPTY.withColor(0x870b32)),
                    DTEAttributeRegistry.RITUAL_MAGIC_POWER,
                    DTEAttributeRegistry.RITUAL_MAGIC_RESIST,
                    SoundRegistry.EVOCATION_CAST,
                    DTEDamageTypes.RITUAL_MAGIC
            ));


    public static final ResourceLocation DARK_RESOURCE = CursedSorcery.id("dark");

    public static final Supplier<SchoolType> DARK = registerSchool(new SchoolType
            (
                    DARK_RESOURCE,
                    DTETags.DARK_FOCUS,
                    Component.translatable("school.cursed_sorcery.dark").withStyle(Style.EMPTY.withColor(0x10003d)),
                    DTEAttributeRegistry.DARK_MAGIC_POWER,
                    DTEAttributeRegistry.DARK_MAGIC_RESIST,
                    SoundRegistry.TELEKINESIS_CAST,
                    DTEDamageTypes.DARK_MAGIC
            ));

}
