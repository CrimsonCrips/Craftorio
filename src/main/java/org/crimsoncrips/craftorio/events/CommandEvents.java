package org.crimsoncrips.craftorio.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
                .then(Commands.literal("contract_refresh_time").requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0)).executes(CommandEvents::runSetContractRefreshTime)))
                .then(Commands.literal("effect_timer_time").requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0)).executes(CommandEvents::runSetEffectTimerTime)))
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

    private static int runGivePoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        BigInteger amount = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));

        if (amount.signum() <= 0) {
            context.getSource().sendFailure(Component.translatable("misc.craftorio.enter_valid_quantity"));
            return 0;
        }

        ServerPlayer sender = context.getSource().getPlayer();
        if (sender != null && sender == target) {
            context.getSource().sendFailure(Component.translatable("misc.craftorio.cannot_give_yourself_points"));
            return 0;
        }

        if (sender != null) {
            BigInteger senderPoints = CraftorioMisc.getPoints(sender);
            if (senderPoints.compareTo(amount) < 0) {
                context.getSource().sendFailure(Component.translatable("misc.craftorio.not_enough_points"));
                return 0;
            }
            CraftorioMisc.setPoints(senderPoints.subtract(amount), sender);
        }

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
        int current = CraftorioMisc.universalBased(serverPlayer.level())
                ? CraftorioMisc.getRandomEffectTime(serverPlayer.level())
                : CraftorioMisc.getRandomEffectTime(serverPlayer);
        current = Math.max(current, 0);
        PacketDistributor.sendToPlayer(serverPlayer, new EffectTimerPacket(nowEnabled, current));

        context.getSource().sendSuccess(() -> Component.translatable(nowEnabled ? "misc.craftorio.effect_timer_enabled" : "misc.craftorio.effect_timer_disabled"), true);
        return 1;
    }

    private static int runSetContractRefreshTime(CommandContext<CommandSourceStack> context) {
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        ServerLevel level = context.getSource().getLevel();
        int ticks = seconds * CraftorioMisc.SECONDS_TO_TICKS;

        ServerPlayer player = context.getSource().getPlayer();
        if (player == null || CraftorioMisc.universalBased(level)) {
            CraftorioMisc.setContractRefreshTime(level, ticks);
        } else {
            CraftorioMisc.setContractRefreshTime(player, ticks);
        }

        context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.contract_refresh_time_set", seconds), true);
        return 1;
    }

    private static int runSetEffectTimerTime(CommandContext<CommandSourceStack> context) {
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        ServerLevel level = context.getSource().getLevel();
        int ticks = seconds * CraftorioMisc.SECONDS_TO_TICKS;

        ServerPlayer player = context.getSource().getPlayer();
        if (player == null || CraftorioMisc.universalBased(level)) {
            CraftorioMisc.setRandomEffectTime(level, ticks);
        } else {
            CraftorioMisc.setRandomEffectTime(player, ticks);
        }

        context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.effect_timer_time_set", seconds), true);
        return 1;
    }

}
