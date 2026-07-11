package net.feshy.cursed_sorcery.events;

import net.feshy.cursed_sorcery.CursedSorcery;


import net.feshy.cursed_sorcery.registries.DTEEntityRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = CursedSorcery.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    @SubscribeEvent
    public static void onAttributeCreateEvent(EntityAttributeCreationEvent event)
    {

//        event.put(DTEEntityRegistry.APOTHIC_SUMMONER.get(), ApothicSummonerEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.APOTHIC_CRUSADER.get(), ApothicCrusaderEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.APOTHIC_ACOLYTE.get(), ApothicAcolyteEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.GAOLER_ENTITY.get(), GaolerEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.ASCENDED_ONE.get(), AscendedOneBoss.createAttributes().build());
//        event.put(DTEEntityRegistry.ASCENDED_ONE_CULTIST.get(), AscendedOneBoss.createAttributes().build());
//        event.put(DTEEntityRegistry.APOSTLE_ENTITY.get(), TheApostleEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.SIGHTLESS_MAW.get(), SightlessMawEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.UNTOLD_BEHEMOTH.get(), UntoldBehemothEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.APOTHIC_TRAITOR.get(), ApothicTraitorEntity.createAttributes().build());

//        event.put(DTEEntityRegistry.BLOOD_CULTIST_CAPTAIN.get(), BloodCultistCaptainEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.ELECTROMANCER_MAGE.get(), ElectromancerEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.BLOOD_CULTIST_MAGE.get(), BloodCultistMageEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.BLOOD_CULTIST_WITCH.get(), BloodCultistWitchEntity.createAttributes().build());
//        event.put(DTEEntityRegistry.BLOOD_MATRIARCH.get(), BloodMatriarchEntity.createAttributes().build());
    }
}
