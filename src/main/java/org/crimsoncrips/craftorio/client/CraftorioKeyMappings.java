package org.crimsoncrips.craftorio.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.ActiveEffectsScreen;
import org.crimsoncrips.craftorio.client.screen.BorderExpandScreen;
import org.crimsoncrips.craftorio.client.screen.ClaimItemPurchaseScreen;
import org.crimsoncrips.craftorio.client.screen.CraftorioHubScreen;
import org.crimsoncrips.craftorio.client.screen.CraftorioSkillTreeScreen;
import org.crimsoncrips.craftorio.client.screen.OwnedContractsScreen;
import org.crimsoncrips.craftorio.networking.PrintScanPacket;
import org.crimsoncrips.craftorio.networking.RequestContractOfferPacket;
import org.crimsoncrips.craftorio.networking.RequestOpenShopPacket;
import org.crimsoncrips.craftorio.networking.RequestOpenValueBrowserPacket;

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

    public static final KeyMapping OPEN_SKILL_TREE = new KeyMapping(
            "key.craftorio.open_skill_tree",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
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
        event.register(OPEN_SKILL_TREE);
        event.register(OPEN_MY_CONTRACTS);
        event.register(OPEN_AVAILABLE_CONTRACTS);
        event.register(OPEN_ITEM_VALUES);
        event.register(OPEN_EXPAND_BORDER_OR_CLAIM);
        event.register(OPEN_ITEM_SHOP);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_HUB.consumeClick()) {
            Minecraft.getInstance().setScreen(new CraftorioHubScreen());
        }
        while (PRINT_SCAN.consumeClick()) {
            PacketDistributor.sendToServer(new PrintScanPacket());
        }
        while (OPEN_ACTIVE_EFFECTS.consumeClick()) {
            Minecraft.getInstance().setScreen(new ActiveEffectsScreen(null));
        }
        while (OPEN_SKILL_TREE.consumeClick()) {
            Minecraft.getInstance().setScreen(new CraftorioSkillTreeScreen());
        }
        while (OPEN_MY_CONTRACTS.consumeClick()) {
            Minecraft.getInstance().setScreen(new OwnedContractsScreen(null));
        }
        while (OPEN_AVAILABLE_CONTRACTS.consumeClick()) {
            PacketDistributor.sendToServer(new RequestContractOfferPacket());
        }
        while (OPEN_ITEM_VALUES.consumeClick()) {
            PacketDistributor.sendToServer(new RequestOpenValueBrowserPacket());
        }
        while (OPEN_EXPAND_BORDER_OR_CLAIM.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && CraftorioMisc.chunkBased(minecraft.level)) {
                minecraft.setScreen(new ClaimItemPurchaseScreen());
            } else {
                minecraft.setScreen(new BorderExpandScreen());
            }
        }
        while (OPEN_ITEM_SHOP.consumeClick()) {
            PacketDistributor.sendToServer(new RequestOpenShopPacket());
        }
    }
}
