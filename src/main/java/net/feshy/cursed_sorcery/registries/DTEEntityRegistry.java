package net.feshy.cursed_sorcery.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.feshy.cursed_sorcery.CursedSorcery;


import net.feshy.cursed_sorcery.entity.spells.crumbling_block.CrumblingBlockEntity;
import net.feshy.cursed_sorcery.entity.spells.cleave.CleaveProjectile;
import net.feshy.cursed_sorcery.entity.spells.dismantle.Dismantle;
import net.feshy.cursed_sorcery.entity.spells.lapse_blue.LapseBlueEntity;
import net.feshy.cursed_sorcery.entity.spells.reversal_red.ReversalRedEntity;
import net.feshy.cursed_sorcery.entity.spells.slice.Slice;


import net.feshy.cursed_sorcery.entity.spells.void_spike.VoidSpikeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DTEEntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CursedSorcery.MOD_ID);


    public static final DeferredHolder<EntityType<?>, EntityType<CrumblingBlockEntity>> CRUMBLING_BLOCK =
            ENTITIES.register("crumbling_block", () -> EntityType.Builder.<CrumblingBlockEntity>of(CrumblingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "crumbling_block").toString())
            );


    // reversal red
    public static final DeferredHolder<EntityType<?>, EntityType<ReversalRedEntity>> REVERSAL_RED =
            ENTITIES.register("reversal_red", () -> EntityType.Builder.<ReversalRedEntity>of(ReversalRedEntity::new, MobCategory.MISC)
                    .sized(5f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "reversal_red").toString())
            );

    // Cleave
    public static final DeferredHolder<EntityType<?>, EntityType<CleaveProjectile>> CLEAVE =
            ENTITIES.register("cleave", () -> EntityType.Builder.<CleaveProjectile>of(CleaveProjectile::new, MobCategory.MISC)
                    .sized(5f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "cleave").toString())
            );

    // lapse blue
    public static final DeferredHolder<EntityType<?>, EntityType<LapseBlueEntity>> LAPSE_BLUE =
            ENTITIES.register("lapse_blue", () -> EntityType.Builder.<LapseBlueEntity>of(LapseBlueEntity::new, MobCategory.MISC)
                    .sized(5f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "lapse_blue").toString())
            );


    // Slice
    public static final DeferredHolder<EntityType<?>, EntityType<Slice>> SLICE =
            ENTITIES.register("slice", () -> EntityType.Builder.<Slice>of(Slice::new, MobCategory.MISC)
                    .sized(5f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "slice").toString())
            );

    // Dismantle
    public static final DeferredHolder<EntityType<?>, EntityType<Dismantle>> DISMANTLE =
            ENTITIES.register("dismantle", () -> EntityType.Builder.<Dismantle>of(Dismantle::new, MobCategory.MISC)
                    .sized(5f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "dismantle").toString())
            );



    // Void Spike
    public static final DeferredHolder<EntityType<?>, EntityType<VoidSpikeEntity>> VOID_SPIKE =
            ENTITIES.register("void_spike", () -> EntityType.Builder.<VoidSpikeEntity>of(VoidSpikeEntity::new, MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "void_spike").toString())
            );




//    // Apothic Summoner
//    public static final DeferredHolder<EntityType<?>, EntityType<ApothicSummonerEntity>> APOTHIC_SUMMONER =
//            ENTITIES.register("apothic_summoner", () -> EntityType.Builder.of(ApothicSummonerEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "apothic_summoner").toString())
//            );
//
//    // Apothic Crusader
//    public static final DeferredHolder<EntityType<?>, EntityType<ApothicCrusaderEntity>> APOTHIC_CRUSADER =
//            ENTITIES.register("apothic_crusader", () -> EntityType.Builder.of(ApothicCrusaderEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "apothic_crusader").toString())
//            );
//
//    // Apothic Acolyte
//    public static final DeferredHolder<EntityType<?>, EntityType<ApothicAcolyteEntity>> APOTHIC_ACOLYTE =
//            ENTITIES.register("apothic_acolyte", () -> EntityType.Builder.of(ApothicAcolyteEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "apothic_acolyte").toString())
//            );
//
//    // Sightless Maw
//    public static final DeferredHolder<EntityType<?>, EntityType<SightlessMawEntity>> SIGHTLESS_MAW =
//            ENTITIES.register("sightless_maw", () -> EntityType.Builder.<SightlessMawEntity>of
//                            (SightlessMawEntity::new, MobCategory.MONSTER).
//                    sized(.6f, 1.8f)
//                    .build(
//                            ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "sightless_maw").toString()
//                    ));
//
//    // Untold Behemoth
//    public static final DeferredHolder<EntityType<?>, EntityType<UntoldBehemothEntity>> UNTOLD_BEHEMOTH =
//            ENTITIES.register("untold_behemoth", () -> EntityType.Builder.<UntoldBehemothEntity>of
//                            (UntoldBehemothEntity::new, MobCategory.MONSTER).
//                    sized(.6f, 1.8f)
//                    .build(
//                            ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "untold_behemoth").toString()
//                    ));
//
//    // The Apostle
//    public static final DeferredHolder<EntityType<?>, EntityType<TheApostleEntity>> APOSTLE_ENTITY =
//            ENTITIES.register("apostle", () -> EntityType.Builder.<TheApostleEntity>of
//                            (TheApostleEntity::new, MobCategory.MONSTER).
//                    sized(.6f, 1.8f)
//                    .build(
//                            ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "apostle").toString()
//                    ));
//
//    // Gaoler
//    public static final DeferredHolder<EntityType<?>, EntityType<GaolerEntity>> GAOLER_ENTITY =
//            ENTITIES.register("gaoler", () -> EntityType.Builder.<GaolerEntity>of
//                            (GaolerEntity::new, MobCategory.MONSTER)
//                    .sized(3.5f, 5f)
//                    .build(
//                            ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "gaoler").toString()
//                    ));
//
//    // The Ascended One
//    public static final DeferredHolder<EntityType<?>, EntityType<AscendedOneBoss>> ASCENDED_ONE =
//            ENTITIES.register("ascended_one", () -> EntityType.Builder.<AscendedOneBoss>of(AscendedOneBoss::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "ascended_one").toString())
//            );
//
//    public static final DeferredHolder<EntityType<?>, EntityType<AscendedOneCultistEntity>> ASCENDED_ONE_CULTIST =
//            ENTITIES.register("ascended_one_cultist", () -> EntityType.Builder.of(AscendedOneCultistEntity::new, MobCategory.MISC)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "ascended_one_cultist").toString())
//            );


    // Apothic Traitor
//    public static final DeferredHolder<EntityType<?>, EntityType<ApothicTraitorEntity>> APOTHIC_TRAITOR =
//            ENTITIES.register("apothic_traitor", () -> EntityType.Builder.of(ApothicTraitorEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "apothic_traitor").toString())
//            );



//    // Blood Cultist Captain
//    public static final DeferredHolder<EntityType<?>, EntityType<BloodCultistCaptainEntity>> BLOOD_CULTIST_CAPTAIN =
//            ENTITIES.register("blood_cultist_captain", () -> EntityType.Builder.of(BloodCultistCaptainEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "blood_cultist_captain").toString())
//            );
//
//    // Blood Cultist Mage
//    public static final DeferredHolder<EntityType<?>, EntityType<BloodCultistMageEntity>> BLOOD_CULTIST_MAGE =
//            ENTITIES.register("blood_cultist_mage", () -> EntityType.Builder.of(BloodCultistMageEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "blood_cultist_mage").toString())
//            );
//
//    // Blood Cultist Witch
//    public static final DeferredHolder<EntityType<?>, EntityType<BloodCultistWitchEntity>> BLOOD_CULTIST_WITCH =
//            ENTITIES.register("blood_cultist_witch", () -> EntityType.Builder.of(BloodCultistWitchEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "blood_cultist_witch").toString())
//            );
//
//    // Blood Matriarch
//    public static final DeferredHolder<EntityType<?>, EntityType<BloodMatriarchEntity>> BLOOD_MATRIARCH =
//            ENTITIES.register("blood_matriarch", () -> EntityType.Builder.of(BloodMatriarchEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "blood_matriarch").toString())
//            );


    // Electromancer
//    public static final DeferredHolder<EntityType<?>, EntityType<ElectromancerEntity>> ELECTROMANCER_MAGE =
//            ENTITIES.register("electromancer", () -> EntityType.Builder.of(ElectromancerEntity::new, MobCategory.MONSTER)
//                    .sized(.6f, 1.8f)
//                    .clientTrackingRange(64)
//                    .build(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "electromancer").toString())
//            );




    public static void register(IEventBus eventBus)
    {
        ENTITIES.register(eventBus);
    }
}
