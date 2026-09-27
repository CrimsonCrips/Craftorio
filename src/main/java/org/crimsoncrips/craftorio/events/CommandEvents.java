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
import org.crimsoncrips.craftorio.networking.skill_tree.OpenRebirthSkillTreeScreenPacket;
import org.crimsoncrips.craftorio.server.rebirth.CraftorioRebirth;
import org.crimsoncrips.craftorio.server.sacrifice.CraftorioSacrifice;

import java.math.BigInteger;
import java.util.List;

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
                .then(Commands.literal("contract_refresh_time").requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0)).executes(CommandEvents::runSetContractRefreshTime)))
                .then(Commands.literal("life").requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(CommandEvents::runSetLife)))
                .then(Commands.literal("effect_timer_time").requires(cs -> cs.hasPermission(3))
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0)).executes(CommandEvents::runSetEffectTimerTime)))
                .then(Commands.literal("sacrifice").requires(cs -> cs.hasPermission(4)).executes(context -> runSacrifice(context, null))
                        .then(Commands.argument("target", EntityArgument.player()).executes(context -> runSacrifice(context, EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("rebirth").requires(cs -> cs.hasPermission(3)).executes(context -> runRebirth(context, null))
                        .then(Commands.argument("target", EntityArgument.player()).executes(context -> runRebirth(context, EntityArgument.getPlayer(context, "target")))))
        );


    }

    private static int runSetLife(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        int lifeAmount = IntegerArgumentType.getInteger(context, "amount");
        CraftorioMisc.setLife(lifeAmount,player);
        return 1;
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

    private static int runSacrifice(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        ServerPlayer player = target != null ? target : context.getSource().getPlayer();
        if (player == null || !CraftorioSacrifice.enter(player, true)) {
            context.getSource().sendFailure(Component.translatable("misc.craftorio.command_sacrifice_failed"));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.command_sacrifice_started", player.getDisplayName()), true);
        return 1;
    }

    private static int runRebirth(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        ServerPlayer player = target != null ? target : context.getSource().getPlayer();
        if (player == null) return 0;

        CraftorioRebirth.forceRebirth(player);
        List<ServerPlayer> reborn = CraftorioMisc.universalBased(CraftorioMisc.universalLevel(player))
                ? context.getSource().getServer().getPlayerList().getPlayers() : List.of(player);
        for (ServerPlayer rebornPlayer : reborn) {
            PacketDistributor.sendToPlayer(rebornPlayer, new OpenRebirthSkillTreeScreenPacket());
        }
        context.getSource().sendSuccess(() -> Component.translatable("misc.craftorio.command_rebirth_done", player.getDisplayName()), true);
        return 1;
    }

}
