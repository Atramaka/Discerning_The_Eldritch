package net.feshy.cursed_sorcery.networking.devour;

import net.feshy.cursed_sorcery.CursedSorcery;
import net.feshy.cursed_sorcery.networking.DTEAttachmentSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GetSyncDevourStacksPacket implements CustomPacketPayload {
    public static final Type<GetSyncDevourStacksPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "get_devour_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GetSyncDevourStacksPacket> STREAM_CODEC = CustomPacketPayload.codec(GetSyncDevourStacksPacket::write, GetSyncDevourStacksPacket::new);

    private final int devourStacks;

    public GetSyncDevourStacksPacket(LivingEntity entity)
    {
        devourStacks = DTEAttachmentSync.getDevour(entity);
    }

    public GetSyncDevourStacksPacket(RegistryFriendlyByteBuf buf)
    {
        devourStacks = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(devourStacks);
    }

    public static void handle(GetSyncDevourStacksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> DTEAttachmentSync.setDevour(packet.devourStacks, context.player()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
