package org.crimsoncrips.craftorio.networking.devtools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffects;

public record DevClaimEntryPacket(boolean effect, ResourceLocation id) implements CustomPacketPayload {

    public static final Type<DevClaimEntryPacket> TYPE = new Type<>(Craftorio.prefix("dev_claim_entry_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DevClaimEntryPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DevClaimEntryPacket::effect,
            ResourceLocation.STREAM_CODEC, DevClaimEntryPacket::id,
            DevClaimEntryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DevClaimEntryPacket message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || !player.isCreative()) return;

            Component name;
            if (message.effect()) {
                CraftorioEffects effect = player.registryAccess().registryOrThrow(CraftorioEffects.REGISTRY_KEY).get(message.id());
                if (effect == null) return;
                CraftorioMisc.grantEffect(player, message.id());
                name = Component.translatable(effect.getNameKey());
            } else {
                CraftorioContract contract = player.registryAccess().registryOrThrow(CraftorioContract.REGISTRY_KEY).get(message.id());
                if (contract == null) return;
                CraftorioMisc.grantContract(player, message.id());
                name = Component.translatable(contract.getName());
            }

            player.playNotifySound(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.translatable("misc.craftorio.dev_claimed", name).withStyle(ChatFormatting.GREEN));
        });
    }
}
