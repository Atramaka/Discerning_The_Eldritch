package net.acetheeldritchking.discerning_the_eldritch.registries;

import net.acetheeldritchking.discerning_the_eldritch.DiscerningTheEldritch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class DTEDamageTypes {
    public static ResourceKey<DamageType> register(String name)
    {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(ResourceLocation.fromNamespaceAndPath(DiscerningTheEldritch.MOD_ID, name).toString()));
    }

    // Void
    public static final ResourceKey<DamageType> DARK_MAGIC = register("dark_magic");

    // Void
    public static final ResourceKey<DamageType> CURSE_MAGIC = register("curse_magic");

    // Ritual
    public static final ResourceKey<DamageType> RITUAL_MAGIC = register("ritual_magic");

    // Razor
    public static final ResourceKey<DamageType> RAZOR_DAMAGE = register("razor_damage");

    // Gore Bile
    public static final ResourceKey<DamageType> GORE_BILE = register("gore_bile");

    // Blood Rot
    public static final ResourceKey<DamageType> BLOOD_ROT = register("blood_rot");

    // Devoured
    public static final ResourceKey<DamageType> DEVOURED = register("devoured");


    public static void bootstrap(BootstrapContext<DamageType> context)
    {
        context.register(RITUAL_MAGIC, new DamageType(RITUAL_MAGIC.location().getPath(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0F));

        context.register(DARK_MAGIC, new DamageType(DARK_MAGIC.location().getPath(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0F));

        context.register(CURSE_MAGIC, new DamageType(CURSE_MAGIC.location().getPath(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0F));

    }
}
