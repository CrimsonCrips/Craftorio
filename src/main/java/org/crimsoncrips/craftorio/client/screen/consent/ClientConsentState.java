package org.crimsoncrips.craftorio.client.screen.consent;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
import org.crimsoncrips.craftorio.networking.consent.ConsentStatusPacket;

import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class ClientConsentState {

    private static final Map<ConsentKind, ConsentStatusPacket> statuses = new EnumMap<>(ConsentKind.class);
    private static final Map<ConsentKind, Long> deadlines = new EnumMap<>(ConsentKind.class);

    private ClientConsentState() {}

    public static void update(ConsentStatusPacket status) {
        if (status.active()) {
            statuses.put(status.kind(), status);
            deadlines.put(status.kind(), Util.getMillis() + status.remainingMs());
            return;
        }

        statuses.remove(status.kind());
        deadlines.remove(status.kind());
        if (Minecraft.getInstance().screen instanceof CraftorioConsentWaitScreen screen && screen.kind() == status.kind()) {
            screen.closeFromServer();
        }
    }

    public static ConsentStatusPacket status(ConsentKind kind) {
        return statuses.get(kind);
    }

    public static long remainingMs(ConsentKind kind) {
        Long deadline = deadlines.get(kind);
        return deadline == null ? -1L : Math.max(0L, deadline - Util.getMillis());
    }

    public static void clear() {
        statuses.clear();
        deadlines.clear();
    }

    public static boolean othersOnline() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getConnection() != null && minecraft.getConnection().getOnlinePlayers().size() > 1;
    }
}
