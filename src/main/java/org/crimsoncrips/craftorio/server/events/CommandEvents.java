package org.crimsoncrips.craftorio.server.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.EffectTimerPacket;
import org.crimsoncrips.craftorio.registries.shipment.CraftorioShipmentContract;

import java.math.BigInteger;

import static org.crimsoncrips.craftorio.CraftorioMisc.pointThreshold;


public class CommandEvents {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("craftorio").then(
                        Commands.literal("points")
                                .then(Commands.literal("add").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(ctx -> modifyPoints(ctx, PointsOp.ADD))))
                                .then(Commands.literal("set").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(ctx -> modifyPoints(ctx, PointsOp.SET))))
                                .then(Commands.literal("subtract").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(ctx -> modifyPoints(ctx, PointsOp.SUBTRACT))))
                                .then(Commands.literal("give").then(Commands.argument("target", EntityArgument.player()).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runGivePoints)))))
                .then(Commands.literal("toggle_effect_timer").requires(cs -> cs.hasPermission(4)).executes(CommandEvents::runToggleEffectTimer))
                .then(Commands.literal("check_contracts").executes(CommandEvents::runCheckContracts))
        );


    }


    private enum PointsOp { ADD, SET, SUBTRACT }

    private static int modifyPoints(CommandContext<CommandSourceStack> context, PointsOp op) {
        ServerPlayer player = context.getSource().getPlayer();
        BigInteger amount = CraftorioMisc.toBigInteger(StringArgumentType.getString(context, "amount"));
        BigInteger current = CraftorioMisc.getPoints(player);

        BigInteger result = switch (op) {
            case ADD -> current.add(amount);
            case SET -> amount;
            case SUBTRACT -> current.subtract(amount);
        };

        if (result.compareTo(pointThreshold()) >= 0) {
            context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.too_much_value"), true);
        }
        CraftorioMisc.setPoints(result, player);
        return 1;
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

    private static int runGivePoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        BigInteger amount = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));
        BigInteger newTotal = CraftorioMisc.getPoints(target).add(amount);

        if (newTotal.compareTo(pointThreshold()) >= 0) {
            context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.too_much_value"), true);
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
