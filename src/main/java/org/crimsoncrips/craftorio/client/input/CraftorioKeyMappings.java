package org.crimsoncrips.craftorio.client.input;

import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.client.screen.devtools.structure.StructureWandScreen;
import org.crimsoncrips.craftorio.client.screen.schematic.SchematicScreen;
import org.crimsoncrips.craftorio.client.screen.hub.CraftorioHubScreen;
import org.crimsoncrips.craftorio.client.screen.hub.CraftorioSacrificeConfirmScreen;
import org.crimsoncrips.craftorio.item.schematic.ContractSchematicItem;
import org.crimsoncrips.craftorio.item.structure.StructureWandItem;
import org.crimsoncrips.craftorio.item.structure.StructureWandSettings;
import org.crimsoncrips.craftorio.networking.devtools.PrintScanPacket;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.client.screen.consent.ClientConsentState;
import org.crimsoncrips.craftorio.client.screen.consent.CraftorioConsentWaitScreen;
import org.crimsoncrips.craftorio.networking.consent.ConsentKind;
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
            InputConstants.KEY_B,
            "key.categories.craftorio"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
        event.register(PRINT_SCAN);
    }

    private static boolean openHeldToolScreen(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.screen != null) return false;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = minecraft.player.getItemInHand(hand);
            if (stack.getItem() instanceof ContractSchematicItem) {
                SchematicData data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
                if (data == null || !CraftorioSchematics.isLinked(minecraft.player, data)) continue;
                minecraft.setScreen(new SchematicScreen(hand));
                return true;
            }
            if (stack.getItem() instanceof StructureWandItem) {
                if (!minecraft.player.canUseGameMasterBlocks()) {
                    minecraft.player.displayClientMessage(Component.translatable("misc.craftorio.structure_wand_no_permission").withStyle(ChatFormatting.RED), true);
                    return true;
                }
                minecraft.setScreen(new StructureWandScreen(hand, stack.getOrDefault(CraftorioDataComponents.STRUCTURE_WAND.get(), StructureWandSettings.DEFAULT)));
                return true;
            }
        }
        return false;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (PRINT_SCAN.consumeClick()) {
            if (!openHeldToolScreen(Minecraft.getInstance())) {
                PacketDistributor.sendToServer(new PrintScanPacket());
            }
        }

        Minecraft minecraft = Minecraft.getInstance();
        boolean inHaven = minecraft.level != null && CraftorioMisc.isInHavenDimension(minecraft.level);

        while (OPEN_HUB.consumeClick()) {
            if (inHaven) {
                if (minecraft.player != null && minecraft.screen == null) {
                    if (minecraft.player.getData(CraftorioDataAttachments.SACRIFICE_PENDING) && !minecraft.player.getData(CraftorioDataAttachments.SACRIFICE_WAITING)) {
                        minecraft.setScreen(ClientConsentState.status(ConsentKind.SACRIFICE) != null
                                ? new CraftorioConsentWaitScreen(ConsentKind.SACRIFICE, false)
                                : new CraftorioSacrificeConfirmScreen());
                    }
                }
                continue;
            }
            minecraft.setScreen(ClientConsentState.status(ConsentKind.REBIRTH) != null
                    ? new CraftorioConsentWaitScreen(ConsentKind.REBIRTH, false)
                    : new CraftorioHubScreen());
        }
    }
}
