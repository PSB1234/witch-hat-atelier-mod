package net.oshino.witchhatateliermod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.oshino.witchhatateliermod.WitchHatAtelierMod;

/** Client request to persist the drawing currently being edited onto the held paper. */
public record PaperSavePayload(boolean mainHand, String title, String drawing) implements CustomPayload {
    public static final int MAX_TITLE_LENGTH = 64;
    public static final int MAX_DRAWING_LENGTH = 262_144;
    public static final Id<PaperSavePayload> ID = new Id<>(WitchHatAtelierMod.id("save_paper"));
    public static final PacketCodec<RegistryByteBuf, PaperSavePayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeBoolean(payload.mainHand);
                buffer.writeString(payload.title, MAX_TITLE_LENGTH);
                buffer.writeString(payload.drawing, MAX_DRAWING_LENGTH);
            },
            buffer -> new PaperSavePayload(buffer.readBoolean(), buffer.readString(MAX_TITLE_LENGTH),
                    buffer.readString(MAX_DRAWING_LENGTH)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
