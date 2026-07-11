package net.feshy.cursed_sorcery.registries;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.feshy.cursed_sorcery.CursedSorcery;


import net.feshy.cursed_sorcery.spells.curse.*;
import net.feshy.cursed_sorcery.spells.eldritch.*;
import net.feshy.cursed_sorcery.spells.dark.DecimateSpell;
import net.feshy.cursed_sorcery.spells.dark.OblivionSpell;
import net.feshy.cursed_sorcery.spells.dark.TransportationSpell;
import net.feshy.cursed_sorcery.spells.dark.VoidSurfSpell;
import net.feshy.cursed_sorcery.spells.evocation.BoogieWoogieSpell;

import net.feshy.cursed_sorcery.spells.ritual.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY;

public class SpellRegistries {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_KEY, CursedSorcery.MOD_ID);

    public static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }


    // ADDED SPELLS BY FESHY

    public static final Supplier<AbstractSpell> DISMANTLE = registerSpell(new DismantleSpell());
    public static final Supplier<AbstractSpell> CLEAVE = registerSpell(new CleaveSpell());
    public static final Supplier<AbstractSpell> SLICE = registerSpell(new SliceSpell());
    public static final Supplier<AbstractSpell> GROUND_CLEAVE = registerSpell(new GroundCleaveSpell());

    public static final Supplier<AbstractSpell> REVERSAL_RED = registerSpell(new RedExplodeSpell());

    public static final Supplier<AbstractSpell> LAPSE_BLUE = registerSpell(new BlueSpell());



    // Void Surf - Step style spell
    public static final Supplier<AbstractSpell> VOID_SURF = registerSpell(new VoidSurfSpell());

    public static final Supplier<AbstractSpell> DECIMATE = registerSpell(new DecimateSpell());

    public static final Supplier<AbstractSpell> CONQUERORS_FLIGHT = registerSpell(new ConquerorsFlightSpell());



    public static final Supplier<AbstractSpell> OBLIVION = registerSpell(new OblivionSpell());

    public static final Supplier<AbstractSpell> TRANSPORTATION = registerSpell(new TransportationSpell());

    /***
     * Eldritch Spells
     */




    /***
     * Ender Spells
     */

    // Hocus Pocus - Tether to an entity on the first recast. On the second, summon that entity to your position


    /***
     * Blood Spells
     */

    // Bloodlust - Damage dealt by user gets turned into health; lifesteal on hit

    // Vein Ripper - Blood melee spell, slash upwards, inflicting blood rot on hit and giving the caster vigor


    /***
     * Evocation Spells
     */



    /***
     * Fire Spells
     */



    /***
     * Holy Spells
     */



    /***
     * Ice Spells
     */




    /***
     * Lightning Spells
     */



    /***
     * Ritual Spells
     */




    public static void register(IEventBus eventBus)
    {
        SPELLS.register(eventBus);
    }
}
