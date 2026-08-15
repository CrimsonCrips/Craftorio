package org.crimsoncrips.craftorio.server.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static org.crimsoncrips.craftorio.Craftorio.LAND_POINTS;

public class CommandEvents {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

         dispatcher.register(Commands.literal("craftorio").then(
                Commands.literal("points")
                        .then(Commands.literal("add").then(Commands.argument("amount",LongArgumentType.longArg()).executes(CommandEvents::runAddPoints)))
                        .then(Commands.literal("set").then(Commands.argument("amount",LongArgumentType.longArg()).executes(CommandEvents::runSetPoints)))
                        .then(Commands.literal("subtract").then(Commands.argument("amount",LongArgumentType.longArg()).executes(CommandEvents::runSubtractPoints)))
                        .then(Commands.literal("show_amount").executes(CommandEvents::runShowPoints)))
        );

    }


    private static int runAddPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        long points = serverLevel.getData(LAND_POINTS);
        serverLevel.setData(LAND_POINTS,points + LongArgumentType.getLong(context,"amount"));
        return 1;
    }

    private static int runSetPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        serverLevel.setData(LAND_POINTS, LongArgumentType.getLong(context,"amount"));
        return 1;
    }

    private static int runSubtractPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        long points = serverLevel.getData(LAND_POINTS);
        serverLevel.setData(LAND_POINTS,points - LongArgumentType.getLong(context,"amount"));
        return 1;
    }
    private static int runShowPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();

        long points = serverLevel.getData(LAND_POINTS);
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(points)), true);
        return 1;
    }

}