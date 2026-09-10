package org.crimsoncrips.craftorio.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientContractOfferState {

    private static boolean available = false;

    private ClientContractOfferState() {
    }

    public static void setAvailable(boolean value) {
        available = value;
    }

    public static boolean isAvailable() {
        return available;
    }
}
