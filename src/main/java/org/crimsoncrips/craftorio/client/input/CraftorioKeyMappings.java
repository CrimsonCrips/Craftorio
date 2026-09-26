package org.crimsoncrips.craftorio.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.contract.OwnedContractsScreen;
import org.crimsoncrips.craftorio.client.screen.effect.ActiveEffectsScreen;
import org.crimsoncrips.craftorio.client.screen.hub.CraftorioHubScreen;
import org.crimsoncrips.craftorio.client.screen.hub.CraftorioSacrificeConfirmScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.BorderExpandScreen;
import org.crimsoncrips.craftorio.client.screen.purchase.ClaimItemPurchaseScreen;
import org.crimsoncrips.craftorio.networking.contract.RequestContractOfferPacket;
import org.crimsoncrips.craftorio.networking.devtools.PrintScanPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.networking.shop.RequestOpenValueBrowserPacket;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;

public class CraftorioKeyMappings {

    public static final KeyMapping OPEN_HUB = new KeyMapping(
            "key.craftorio.open_hub",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            "key.categories.craftorio"
    );

    public static final KeyMapping PRINT_SCAN = new KeyMapping(
            "key.craftorio.print_scan",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            "key.categories.craftorio"
    );

    public static final KeyMapping OPEN_ACTIVE_EFFECTS = new KeyMapping(
            "key.craftorio.open_active_effects",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            "key.categories.craftorio"
    );

    public static final KeyMapping OPEN_MY_CONTRACTS = new KeyMapping(
            "key.craftorio.open_my_contracts",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_N,
            "key.categories.craftorio"
    );

    public static final KeyMapping OPEN_AVAILABLE_CONTRACTS = new KeyMapping(
            "key.craftorio.open_available_contracts",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_M,
            "key.categories.craftorio"
    );

    public static final KeyMapping OPEN_ITEM_VALUES = new KeyMapping(
            "key.craftorio.open_item_values",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.categories.craftorio"
    );

    public static final KeyMapping OPEN_EXPAND_BORDER_OR_CLAIM = new KeyMapping(
            "key.craftorio.open_expand_border_or_claim",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_B,
            "key.categories.craftorio"
    );

    public static final KeyMapping OPEN_ITEM_SHOP = new KeyMapping(
            "key.craftorio.open_item_shop",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_U,
            "key.categories.craftorio"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
        event.register(PRINT_SCAN);
        event.register(OPEN_ACTIVE_EFFECTS);
        event.register(OPEN_MY_CONTRACTS);
        event.register(OPEN_AVAILABLE_CONTRACTS);
        event.register(OPEN_ITEM_VALUES);
        event.register(OPEN_EXPAND_BORDER_OR_CLAIM);
        event.register(OPEN_ITEM_SHOP);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (PRINT_SCAN.consumeClick()) {
            PacketDistributor.sendToServer(new PrintScanPacket());
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean inHaven = minecraft.level != null && CraftorioMisc.isInHavenDimension(minecraft.level);

        while (OPEN_HUB.consumeClick()) {
            if (inHaven) {
                if (minecraft.player != null && minecraft.screen == null) {
                    if (minecraft.player.getData(CraftorioDataAttachments.SACRIFICE_PENDING) && !minecraft.player.getData(CraftorioDataAttachments.SACRIFICE_WAITING)) {
                        minecraft.setScreen(new CraftorioSacrificeConfirmScreen());
                    }
                }
                continue;
            }
            minecraft.setScreen(new CraftorioHubScreen());
        }
        while (OPEN_ACTIVE_EFFECTS.consumeClick()) {
            if (inHaven) continue;
            minecraft.setScreen(new ActiveEffectsScreen(null));
        }
        while (OPEN_MY_CONTRACTS.consumeClick()) {
            if (inHaven) continue;
            minecraft.setScreen(new OwnedContractsScreen(null));
        }
        while (OPEN_AVAILABLE_CONTRACTS.consumeClick()) {
            if (inHaven) continue;
            PacketDistributor.sendToServer(new RequestContractOfferPacket());
        }
        while (OPEN_ITEM_VALUES.consumeClick()) {
            if (inHaven) continue;
            PacketDistributor.sendToServer(new RequestOpenValueBrowserPacket());
        }
        while (OPEN_EXPAND_BORDER_OR_CLAIM.consumeClick()) {
            if (inHaven || minecraft.level == null) continue;
            if (CraftorioMisc.chunkBased(minecraft.level)) {
                minecraft.setScreen(new ClaimItemPurchaseScreen());
            } else {
                minecraft.setScreen(new BorderExpandScreen());
            }
        }
        while (OPEN_ITEM_SHOP.consumeClick()) {
            if (inHaven) continue;
            PacketDistributor.sendToServer(new RequestOpenShopPacket());
        }
    }
}
