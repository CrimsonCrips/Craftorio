package org.crimsoncrips.craftorio;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.block.entity.CraftorioBlockEntityTypes;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.client.CraftorioClientConfig;
import org.crimsoncrips.craftorio.client.CraftorioKeyMappings;
import org.crimsoncrips.craftorio.client.compat.XaeroWorldMapCompat;
import org.crimsoncrips.craftorio.datagen.CraftorioDatagen;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffectTypes;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.loot.CraftorioLootModifiers;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementPoints;
import org.crimsoncrips.craftorio.server.CraftorioAdvancementMultipliers;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.CraftorioServerConfig;
import org.crimsoncrips.craftorio.events.CommandEvents;
import org.crimsoncrips.craftorio.networking.PacketRegistration;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.server.unlocks.CraftorioUnlockedItemsManager;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.slf4j.Logger;

import java.util.Locale;

@SuppressWarnings("Deprecated")
@Mod(Craftorio.MODID)
public class Craftorio {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "craftorio";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CraftorioServerConfig SERVER_CONFIG;
    private static final ModConfigSpec SERVER_CONFIG_SPEC;

    public static final CraftorioClientConfig CLIENT_CONFIG;
    private static final ModConfigSpec CLIENT_CONFIG_SPEC;


    public static final CraftorioUnlockedItemsManager UNLOCKED_ITEMS = new CraftorioUnlockedItemsManager();

    static {
        final Pair<CraftorioServerConfig, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(CraftorioServerConfig::new);
        SERVER_CONFIG = serverPair.getLeft();
        SERVER_CONFIG_SPEC = serverPair.getRight();

        final Pair<CraftorioClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(CraftorioClientConfig::new);
        CLIENT_CONFIG = clientPair.getLeft();
        CLIENT_CONFIG_SPEC = clientPair.getRight();
    }

    public Craftorio(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        CraftorioDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(CraftorioDatagen::generateData);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new CraftorioAdvancementPoints()));
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new CraftorioAdvancementMultipliers()));

        NeoForge.EVENT_BUS.register(new CommandEvents());
        NeoForge.EVENT_BUS.register(new ServerEvents());
        NeoForge.EVENT_BUS.register(UNLOCKED_ITEMS);

        modEventBus.addListener(new PacketRegistration()::setupPackets);
        modEventBus.addListener(CraftorioDataMaps::registerDataMaps);
        modEventBus.addListener(CraftorioBlockEntityTypes::registerCapabilities);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(new ClientEvents()::registerScreens);
            modEventBus.addListener(ClientEvents::showPoints);
            modEventBus.addListener(ClientEvents::showActiveEffects);
            modEventBus.addListener(ClientEvents::showEffectTimer);
            modEventBus.addListener(ClientEvents::showToasts);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderBorders);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderClaimedChunkBorders);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderPauseMenuIndicators);
            modEventBus.addListener(CraftorioKeyMappings::register);
            NeoForge.EVENT_BUS.addListener(CraftorioKeyMappings::onClientTick);
            NeoForge.EVENT_BUS.addListener(ClientEvents::tickUniversalProgressDisplay);
            NeoForge.EVENT_BUS.addListener(ClientEvents::addCraftorioStatsButton);
            ClientEvents.registerConfigScreen(modContainer);

            if (ModList.get().isLoaded("xaeroworldmap")) {
                NeoForge.EVENT_BUS.addListener(XaeroWorldMapCompat::onClientTick);
                NeoForge.EVENT_BUS.addListener(XaeroWorldMapCompat::renderOverlay);
            }
        }

        CraftorioLootModifiers.MODIFIERS.register(modEventBus);
        CraftorioEffectTypes.TYPES.register(modEventBus);
        CraftorioUpgradeTypes.TYPES.register(modEventBus);
        CraftorioBlocks.BLOCKS.register(modEventBus);
        CraftorioBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        CraftorioItems.ITEMS.register(modEventBus);
        CraftorioMenuTypes.CONTAINERS.register(modEventBus);
        CraftorioDataComponents.COMPONENTS.register(modEventBus);

        //Config
        modContainer.registerConfig(ModConfig.Type.COMMON, SERVER_CONFIG_SPEC, "craftorio-general.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG_SPEC, "craftorio-client.toml");
    }

    private static final String GUI_DIR = "textures/gui/";

    public static ResourceLocation getGuiTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, GUI_DIR + name);
    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name.toLowerCase(Locale.ROOT));
    }

}
