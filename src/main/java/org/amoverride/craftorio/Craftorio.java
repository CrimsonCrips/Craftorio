package org.amoverride.craftorio;

import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
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
import org.amoverride.craftorio.datagen.CraftorioDatagen;
import org.amoverride.craftorio.server.events.CommandEvents;
import org.amoverride.craftorio.server.events.RegistrationEvents;
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
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "craftorio" namespace

    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<Integer>> LAND_POINTS = ATTACHMENT_TYPES.register(
            "land_points", () -> AttachmentType.builder(() -> 100).serialize(Codec.INT).build()
    );

    public static final Supplier<AttachmentType<Unit>> OWNED = ATTACHMENT_TYPES.register(
            "owned", () -> AttachmentType.builder(() -> Unit.INSTANCE).build()
    );

    public Craftorio(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(CraftorioDatagen::generateData);

        NeoForge.EVENT_BUS.register(new CommandEvents());
        modEventBus.addListener(new RegistrationEvents()::setupPackets);



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
