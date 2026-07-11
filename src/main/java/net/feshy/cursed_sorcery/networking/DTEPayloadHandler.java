package net.feshy.cursed_sorcery.networking;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.networking.devour.GetSyncDevourStacksPacket;
import net.feshy.cursed_sorcery.networking.devour.ResetSyncDevourStacksPacket;
import net.feshy.cursed_sorcery.networking.devour.SetSyncDevourStacksPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = CursedSorcery.MOD_ID)
public class DTEPayloadHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(CursedSorcery.MOD_ID).versioned("1.0.0").optional();


        payloadRegistrar.playToClient(SetSyncDevourStacksPacket.TYPE, SetSyncDevourStacksPacket.STREAM_CODEC, SetSyncDevourStacksPacket::handle);
        payloadRegistrar.playToClient(ResetSyncDevourStacksPacket.TYPE, ResetSyncDevourStacksPacket.STREAM_CODEC, ResetSyncDevourStacksPacket::handle);
        payloadRegistrar.playToClient(GetSyncDevourStacksPacket.TYPE, GetSyncDevourStacksPacket.STREAM_CODEC, GetSyncDevourStacksPacket::handle);
    }
}
