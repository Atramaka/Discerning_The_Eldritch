package net.feshy.cursed_sorcery.networking;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SprintBoostPacket() implements CustomPacketPayload {
    public static final Type<SprintBoostPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "sprint_boost")
    );

    public static final StreamCodec<FriendlyByteBuf, SprintBoostPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {}, // Nothing to write
            buf -> new SprintBoostPacket() // Nothing to read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}