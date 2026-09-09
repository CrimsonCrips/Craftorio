package org.crimsoncrips.craftorio.server.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.EffectTimerPacket;
import org.crimsoncrips.craftorio.networking.ExpandScreenPacket;
import org.crimsoncrips.craftorio.networking.OpenClaimShopScreenPacket;
import org.crimsoncrips.craftorio.networking.OpenShopScreenPacket;
import org.crimsoncrips.craftorio.networking.OpenValueBrowserPacket;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;
import org.crimsoncrips.craftorio.server.CraftorioShop;
import org.crimsoncrips.craftorio.server.CraftorioShopMode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.crimsoncrips.craftorio.CraftorioMisc.pointThreshold;


public class CommandEvents {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("craftorio").then(
                        Commands.literal("points")
                                .then(Commands.literal("add").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runAddPoints)))
                                .then(Commands.literal("set").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runSetPoints)))
                                .then(Commands.literal("subtract").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runSubtractPoints)))
                                .then(Commands.literal("give").then(Commands.argument("target", EntityArgument.player()).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runGivePoints)))))
                .then(Commands.literal("check_values").requires(cs -> cs.hasPermission(2)).executes(CommandEvents::runPropertiesCheck))
                .then(Commands.literal("check_contracts").executes(CommandEvents::runCheckContracts))
                .then(Commands.literal("shop").executes(CommandEvents::runOpenShop))
                .then(Commands.literal("claim_shop").executes(CommandEvents::runOpenClaimShop))
                .then(Commands.literal("value_browser").executes(CommandEvents::runOpenValueBrowser))
                .then(Commands.literal("border_expand").executes(CommandEvents::runBorderExpand))
                .then(Commands.literal("toggle_effect_timer").requires(cs -> cs.hasPermission(4)).executes(CommandEvents::runToggleEffectTimer))

        );


    }


    private static int runCheckContracts(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer != null) {
            for (CraftorioShipmentContract shipmentContract : CraftorioMisc.getCraftorioContracts(serverPlayer)){
                context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.contract_info", shipmentContract.getName(), shipmentContract.getTime()), true);
            }
        }
        return 1;
    }

    private static int runPropertiesCheck(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer != null) {
            context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.debug_no_borders", CraftorioMisc.isNoBorders(serverPlayer.level())), true);
            context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.debug_universal", CraftorioMisc.universalBased(serverPlayer.level())), true);
            context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.debug_chunk_based", CraftorioMisc.chunkBased(serverPlayer.level())), true);

        }
        return 1;
    }

    private static int runBorderExpand(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer != null) {
            PacketDistributor.sendToPlayer(serverPlayer, new ExpandScreenPacket(true));
        }
        return 1;
    }

    private static int runOpenShop(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer == null) return 0;

        if (!CraftorioShop.isEnabled()) {
            serverPlayer.sendSystemMessage(Component.translatable("misc.craftorio.shop_disabled"));
            return 0;
        }
        boolean allUnlocked = Craftorio.SERVER_CONFIG.SHOP_MODE.get() == CraftorioShopMode.OPEN;
        List<ResourceLocation> unlocked = allUnlocked ? List.of() : new ArrayList<>(Craftorio.UNLOCKED_ITEMS.getUnlocked(serverPlayer));

        PacketDistributor.sendToPlayer(serverPlayer, new OpenShopScreenPacket(allUnlocked, unlocked));
        return 1;
    }

    private static int runOpenClaimShop(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer == null) return 0;

        PacketDistributor.sendToPlayer(serverPlayer, new OpenClaimShopScreenPacket());
        return 1;
    }

    private static int runOpenValueBrowser(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer == null) return 0;

        PacketDistributor.sendToPlayer(serverPlayer, new OpenValueBrowserPacket());
        return 1;
    }


    private static int runSetPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        BigInteger points = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));

        if (points.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        CraftorioMisc.setPoints(points,context.getSource().getPlayer());
        return 1;
    }

    private static int runAddPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        BigInteger points = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));
        points = points.add(CraftorioMisc.getPoints(context.getSource().getPlayer()));

        if (points.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        CraftorioMisc.setPoints(points,context.getSource().getPlayer());
        return 1;
    }

    private static int runSubtractPoints(CommandContext<CommandSourceStack> context) {
        BigInteger points = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));
        points = points.subtract(CraftorioMisc.getPoints(context.getSource().getPlayer()));

        if (points.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        CraftorioMisc.setPoints(points,context.getSource().getPlayer());
        return 1;
    }

    private static int runGivePoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        BigInteger amount = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));
        BigInteger newTotal = CraftorioMisc.getPoints(target).add(amount);

        if (newTotal.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }

        CraftorioMisc.setPoints(newTotal, target);
        context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.gave_points", amount.toString(), target.getGameProfile().getName()), true);
        return 1;
    }

    private static int runToggleEffectTimer(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer == null) return 0;

        boolean nowEnabled = ServerEvents.toggleEffectTimerViewer(serverPlayer);
        int current = Math.max(CraftorioMisc.getRandomEffectTime(serverPlayer.level()), 0);
        PacketDistributor.sendToPlayer(serverPlayer, new EffectTimerPacket(nowEnabled, current));

        context.getSource().sendSuccess(() -> Component.translatable(nowEnabled ? "misc.craftorio.effect_timer_enabled" : "misc.craftorio.effect_timer_disabled"), true);
        return 1;
    }

}