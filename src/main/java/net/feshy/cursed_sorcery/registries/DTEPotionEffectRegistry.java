package net.feshy.cursed_sorcery.registries;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.effects.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DTEPotionEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CursedSorcery.MOD_ID);

    // Cleave Potion Effect
    public static final DeferredHolder<MobEffect, MobEffect> CLEAVE_POTION_EFFECT = MOB_EFFECTS.register("cleave_effect", CleavePotionEffect::new);

    // Six Eyes Effect
    public static final DeferredHolder<MobEffect, MobEffect> SIX_EYES_EFFECT = MOB_EFFECTS.register("six_eyes_effect", () -> new SixEyeEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));
    // Float Effect
    public static final DeferredHolder<MobEffect, MobEffect> FLOAT_EFFECT = MOB_EFFECTS.register("float_effect", () -> new SixEyeEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));


//    // Silence Potion Effect
//    public static final DeferredHolder<MobEffect, MobEffect> SILENCE_POTION_EFFECT = MOB_EFFECTS.register("silence_potion_effect", SilencePotionEffect::new);
//
//    // Metaphysical Potion Effect
//    public static final DeferredHolder<MobEffect, MobEffect> METAPHYSICAL_POTION_EFFECT = MOB_EFFECTS.register("metaphysical_potion_effect", MetaphysicalPotionEffect::new);
//
//    // Mend Flesh Effect
//    public static final DeferredHolder<MobEffect, MobEffect> MEND_FLESH_EFFECT = MOB_EFFECTS.register("mend_flesh_potion_effect", MendFleshPotionEffect::new);
//
//    // Abracadabra Potion Effect
//    public static final DeferredHolder<MobEffect, MobEffect> ABRACADABRA_EFFECT = MOB_EFFECTS.register("abracadabra_potion_effect", AbracadabraPotionEffect::new);
//
//    // Portent Effect
//    public static final DeferredHolder<MobEffect, MobEffect> PORTENT_EFFECT = MOB_EFFECTS.register("portent_effect", PortentEffect::new);
//
//    // Frostbite Effect
//    public static final DeferredHolder<MobEffect, MobEffect> FROSTBITE_EFFECT = MOB_EFFECTS.register("frostbite_effect", FrostbitePotionEffect::new);
//
//    // Malignant Burn Effect
//    public static final DeferredHolder<MobEffect, MobEffect> MALIGNANT_BURN_EFFECT = MOB_EFFECTS.register("malignant_burn_potion_effect", MalignantBurnEffect::new);
//
//    // Ruin Effect
//
//    // Accursed Effect
//    public static final DeferredHolder<MobEffect, MobEffect> ACCURSED_EFFECT = MOB_EFFECTS.register("accursed_potion_effect", AccursedPotionEffect::new);
//
//    // Blood Rot Effect
//    public static final DeferredHolder<MobEffect, MobEffect> BLOOD_ROT_EFFECT = MOB_EFFECTS.register("blood_rot_potion_effect", BloodRotPotionEffect::new);
//
//    // Devoured Effect
//
//    // Prey Effect
//    public static final DeferredHolder<MobEffect, MobEffect> PREY_POTION_EFFECT = MOB_EFFECTS.register("prey_potion_effect", PreyPotionEffect::new);
//
//    // Predator Effect
//    public static final DeferredHolder<MobEffect, MobEffect> PREDATOR_POTION_EFFECT = MOB_EFFECTS.register("predator_potion_effect", PredatorPotionEffect::new);
//
//    // Scorched Soul Effect
//    public static final DeferredHolder<MobEffect, MobEffect> SCORCHED_SOUL_EFFECT = MOB_EFFECTS.register("scorched_soul_potion_effect", ScorchedSoulPotionEffect::new);

    // Void Surf Effect
    public static final DeferredHolder<MobEffect, MobEffect> VOID_SURF_EFFECT = MOB_EFFECTS.register("void_surf_effect", VoidSurfPotionEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> DESTRUCTIVE_RAMPAGE_EFFECT = MOB_EFFECTS.register("destructive_rampage_effect", DestructiveRampageEffect::new);


    public static void register(IEventBus eventBus)
    {
        MOB_EFFECTS.register(eventBus);
    }
}
