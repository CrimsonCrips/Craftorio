package org.crimsoncrips.craftorio;

import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.*;
import org.crimsoncrips.craftorio.block.CraftorioBlocks;
import org.crimsoncrips.craftorio.client.ClientRegistrationEvents;
import org.crimsoncrips.craftorio.datagen.CraftorioDatagen;
import org.crimsoncrips.craftorio.datagen.maps.ModDataMaps;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.server.events.CommandEvents;
import org.crimsoncrips.craftorio.server.events.RegistrationEvents;
import org.crimsoncrips.craftorio.server.events.ServerEvents;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.function.Supplier;

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
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<Long>> LAND_POINTS = ATTACHMENT_TYPES.register(
            "land_points", () -> AttachmentType.builder(() -> 100L).serialize(Codec.LONG).sync(ByteBufCodecs.VAR_LONG).build()
    );

    public static final Supplier<AttachmentType<Integer>> AMOUNT_OF_LAND = ATTACHMENT_TYPES.register(
            "amount_of_land", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).sync(ByteBufCodecs.INT).build()
    );

    public static final Supplier<AttachmentType<Unit>> OWNED = ATTACHMENT_TYPES.register(
            "owned", () -> AttachmentType.builder(() -> Unit.INSTANCE).sync(StreamCodec.unit(Unit.INSTANCE)).build()
    );

    public Craftorio(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(CraftorioDatagen::generateData);

        NeoForge.EVENT_BUS.register(new CommandEvents());
        NeoForge.EVENT_BUS.register(new ServerEvents());

        modEventBus.addListener(new RegistrationEvents()::setupPackets);
        modEventBus.addListener(ModDataMaps::registerDataMaps);
        modEventBus.addListener(new ClientRegistrationEvents()::registerScreens);


        CraftorioBlocks.BLOCKS.register(modEventBus);
        CraftorioBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CraftorioItems.ITEMS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
    private static final String GUI_DIR = "textures/gui/";

    public static ResourceLocation getGuiTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, GUI_DIR + name);
    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name.toLowerCase(Locale.ROOT));
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
