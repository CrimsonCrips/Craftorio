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
import org.crimsoncrips.craftorio.block.blockentity.CraftorioBlockEntityTypes;
import org.crimsoncrips.craftorio.client.schematic.ClientSchematics;
import org.crimsoncrips.craftorio.client.schematic.CraftorioChronosphere;
import org.crimsoncrips.craftorio.client.schematic.CraftorioSchematicRenderer;
import org.crimsoncrips.craftorio.client.compat.IrisCompat;
import org.crimsoncrips.craftorio.client.compat.XaeroWorldMapCompat;
import org.crimsoncrips.craftorio.client.config.CraftorioClientConfig;
import org.crimsoncrips.craftorio.client.input.CraftorioKeyMappings;
import org.crimsoncrips.craftorio.client.render.CraftorioRebirthHealthEffect;
import org.crimsoncrips.craftorio.client.render.CraftorioScreenFade;
import org.crimsoncrips.craftorio.server.config.CraftorioDevServerOptions;
import org.crimsoncrips.craftorio.client.render.CraftorioHavenTransition;
import org.crimsoncrips.craftorio.client.render.CraftorioPlayerDissolve;
import org.crimsoncrips.craftorio.client.render.CraftorioShaders;
import org.crimsoncrips.craftorio.client.screen.CraftorioScreenScroll;
import org.crimsoncrips.craftorio.client.render.CraftorioShatterEffect;
import org.crimsoncrips.craftorio.datagen.CraftorioDatagen;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;
import org.crimsoncrips.craftorio.events.ClientEvents;
import org.crimsoncrips.craftorio.events.CommandEvents;
import org.crimsoncrips.craftorio.events.ServerEvents;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.item.ScannerStickItem;
import org.crimsoncrips.craftorio.loot.CraftorioLootModifiers;
import org.crimsoncrips.craftorio.networking.PacketRegistration;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.registries.CraftorioMenuTypes;
import org.crimsoncrips.craftorio.registries.effect.CraftorioEffectTypes;
import org.crimsoncrips.craftorio.server.advancement.CraftorioAdvancementMultipliers;
import org.crimsoncrips.craftorio.server.advancement.CraftorioAdvancementPoints;
import org.crimsoncrips.craftorio.server.advancement.CraftorioPointsAdvancements;
import org.crimsoncrips.craftorio.server.config.CraftorioServerConfig;
import org.crimsoncrips.craftorio.server.loan.CraftorioLoanEffects;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.unlocks.CraftorioUnlockedItemsManager;
import org.crimsoncrips.craftorio.skill_tree.CraftorioUpgradeTypes;
import org.crimsoncrips.craftorio.worldgen.CraftorioFeatures;
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
    public static final ModConfigSpec SERVER_CONFIG_SPEC;
    public static final String SERVER_CONFIG_FILE = "craftorio-general.toml";

    public static final CraftorioClientConfig CLIENT_CONFIG;
    public static final ModConfigSpec CLIENT_CONFIG_SPEC;


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
        if (FMLEnvironment.dist.isDedicatedServer()) {
            CraftorioDevServerOptions.deleteWorldIfRequested();
        }
        modEventBus.addListener(CraftorioPointsAdvancements::registerTrigger);

        // Register the commonSetup method for modloading
        CraftorioDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(CraftorioDatagen::generateData);

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new CraftorioAdvancementPoints()));
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new CraftorioAdvancementMultipliers()));
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new CraftorioLoanEffects()));

        NeoForge.EVENT_BUS.register(new CommandEvents());
        NeoForge.EVENT_BUS.register(new ServerEvents());
        NeoForge.EVENT_BUS.register(UNLOCKED_ITEMS);
        NeoForge.EVENT_BUS.addListener(ScannerStickItem::onLeftClickBlock);

        modEventBus.addListener(new PacketRegistration()::setupPackets);
        modEventBus.addListener(CraftorioDataMaps::registerDataMaps);
        modEventBus.addListener(CraftorioBlockEntityTypes::registerCapabilities);
        modEventBus.addListener(CraftorioItems::addCreative);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(new ClientEvents()::registerScreens);
            modEventBus.addListener(ClientEvents::showPoints);
            modEventBus.addListener(CraftorioShaders::register);
            modEventBus.addListener(CraftorioShatterEffect::registerLayer);
            NeoForge.EVENT_BUS.addListener(CraftorioShatterEffect::renderOverScreen);
            NeoForge.EVENT_BUS.addListener(CraftorioShatterEffect::onLoggingOut);
            NeoForge.EVENT_BUS.addListener(CraftorioShatterEffect::onLoggingIn);
            NeoForge.EVENT_BUS.addListener(CraftorioShatterEffect::onScreenOpening);
            NeoForge.EVENT_BUS.addListener(CraftorioScreenScroll::onScreenInit);
            NeoForge.EVENT_BUS.addListener(CraftorioScreenScroll::onMouseScrolled);
            modEventBus.addListener(CraftorioRebirthHealthEffect::registerLayer);
            NeoForge.EVENT_BUS.addListener(CraftorioRebirthHealthEffect::renderOverScreen);
            NeoForge.EVENT_BUS.addListener(CraftorioRebirthHealthEffect::hideVanillaHearts);
            NeoForge.EVENT_BUS.addListener(CraftorioRebirthHealthEffect::onLoggingOut);
            modEventBus.addListener(CraftorioScreenFade::registerLayer);
            NeoForge.EVENT_BUS.addListener(CraftorioScreenFade::renderOverScreen);
            NeoForge.EVENT_BUS.addListener(CraftorioScreenFade::onLoggingOut);
            modEventBus.addListener(CraftorioHavenTransition::registerLayer);
            NeoForge.EVENT_BUS.addListener(CraftorioHavenTransition::hideLoadingScreen);
            NeoForge.EVENT_BUS.addListener(CraftorioHavenTransition::renderOverScreen);
            NeoForge.EVENT_BUS.addListener(CraftorioHavenTransition::onLoggingOut);
            NeoForge.EVENT_BUS.addListener(CraftorioPlayerDissolve::onRenderPlayer);
            NeoForge.EVENT_BUS.addListener(CraftorioPlayerDissolve::onClientTick);
            NeoForge.EVENT_BUS.addListener(CraftorioPlayerDissolve::onEntityLeave);
            NeoForge.EVENT_BUS.addListener(CraftorioPlayerDissolve::onClone);
            NeoForge.EVENT_BUS.addListener(CraftorioPlayerDissolve::onLoggingOut);
            NeoForge.EVENT_BUS.addListener(CraftorioSchematicRenderer::onRenderLevel);
            NeoForge.EVENT_BUS.addListener(ClientSchematics::onLoggingOut);
            NeoForge.EVENT_BUS.addListener(CraftorioChronosphere::onRenderLevel);
            NeoForge.EVENT_BUS.addListener(CraftorioChronosphere::onLoggingOut);
            modEventBus.addListener(ClientEvents::showToasts);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderScanBox);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderBorders);
            NeoForge.EVENT_BUS.addListener(ClientEvents::renderClaimedChunkBorders);
            modEventBus.addListener(CraftorioKeyMappings::register);
            NeoForge.EVENT_BUS.addListener(CraftorioKeyMappings::onClientTick);
            NeoForge.EVENT_BUS.addListener(ClientEvents::tickUniversalProgressDisplay);
            NeoForge.EVENT_BUS.addListener(ClientEvents::addCraftorioStatsButton);
            NeoForge.EVENT_BUS.addListener(ClientEvents::addCraftorioWorldCreationButton);
            NeoForge.EVENT_BUS.addListener(ClientEvents::repositionWorldCreationButton);
            modEventBus.addListener(ClientEvents::registerDimensionEffects);
            ClientEvents.registerConfigScreen(modContainer);

            if (ModList.get().isLoaded("xaeroworldmap")) {
                NeoForge.EVENT_BUS.addListener(XaeroWorldMapCompat::onClientTick);
                NeoForge.EVENT_BUS.addListener(XaeroWorldMapCompat::renderOverlay);
            }

            if (ModList.get().isLoaded("iris")) {
                NeoForge.EVENT_BUS.addListener(IrisCompat::onClientTick);
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
        CraftorioFeatures.FEATURES.register(modEventBus);

        //Config
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG_SPEC, SERVER_CONFIG_FILE);
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
