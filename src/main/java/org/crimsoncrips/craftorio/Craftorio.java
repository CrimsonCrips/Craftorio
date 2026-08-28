package org.crimsoncrips.craftorio;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.block.entity.CraftorioBlockEntityTypes;
import org.crimsoncrips.craftorio.client.ClientEvents;
import org.crimsoncrips.craftorio.client.CraftorioClientConfig;
import org.crimsoncrips.craftorio.datagen.CraftorioDatagen;
import org.crimsoncrips.craftorio.datagen.maps.CraftorioDataMaps;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.server.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.server.CraftorioServerConfig;
import org.crimsoncrips.craftorio.server.events.CommandEvents;
import org.crimsoncrips.craftorio.server.events.RegistrationEvents;
import org.crimsoncrips.craftorio.server.events.ServerEvents;
import org.slf4j.Logger;

import java.util.Locale;

@SuppressWarnings("Deprecated")
// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Craftorio.MODID)
public class Craftorio {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "craftorio";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "craftorio" namespace

    // Create the DeferredRegister for attachment types

    public static final CraftorioServerConfig SERVER_CONFIG;
    private static final ModConfigSpec SERVER_CONFIG_SPEC;

    public static final CraftorioClientConfig CLIENT_CONFIG;
    private static final ModConfigSpec CLIENT_CONFIG_SPEC;

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

        NeoForge.EVENT_BUS.register(new CommandEvents());
        NeoForge.EVENT_BUS.register(new ServerEvents());

        modEventBus.addListener(new RegistrationEvents()::setupPackets);
        modEventBus.addListener(CraftorioDataMaps::registerDataMaps);
        modEventBus.addListener(new ClientEvents()::registerScreens);
        modEventBus.addListener(ClientEvents::showPoints);

        CraftorioBlocks.BLOCKS.register(modEventBus);
        CraftorioBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        CraftorioItems.ITEMS.register(modEventBus);
        CraftorioMenuTypes.CONTAINERS.register(modEventBus);


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
