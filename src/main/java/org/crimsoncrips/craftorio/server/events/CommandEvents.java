package org.crimsoncrips.craftorio.server.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.crimsoncrips.craftorio.CraftorioMisc;

import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.AMOUNT_OF_LAND;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.POINTS;


public class CommandEvents {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("craftorio").then(
                        Commands.literal("points")
                                .then(Commands.literal("valueless_items").executes(CommandEvents::runValueless))
                                .then(Commands.literal("add").then(Commands.argument("amount",LongArgumentType.longArg()).executes(CommandEvents::runAddPoints)))
                                .then(Commands.literal("set").then(Commands.argument("amount",LongArgumentType.longArg()).executes(CommandEvents::runSetPoints)))
                                .then(Commands.literal("subtract").then(Commands.argument("amount",LongArgumentType.longArg()).executes(CommandEvents::runSubtractPoints)))
                                .then(Commands.literal("check_amount").executes(CommandEvents::runShowPoints))).then(
                        Commands.literal("land")
                                .then(Commands.literal("check_amount").executes(CommandEvents::runShowLandAmount))

                )
        );

    }

    private static int runValueless(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();

        BuiltInRegistries.ITEM.forEach(item -> {
            int points = CraftorioMisc.checkValue(new ItemStack(item));
            if (points <= 0){
                context.getSource().sendSuccess(() -> Component.literal(item.getDefaultInstance().getDisplayName().getString() + " " + points), true);
            }
        });
        return 1;
    }

    private static int runAddPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        long points = LongArgumentType.getLong(context,"amount");
        CraftorioMisc.addPoints(serverLevel,points);
        return 1;
    }

    private static int runSetPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        serverLevel.setData(POINTS, LongArgumentType.getLong(context,"amount"));
        return 1;
    }

    private static int runSubtractPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        long points = serverLevel.getData(POINTS);
        serverLevel.setData(POINTS,points - LongArgumentType.getLong(context,"amount"));
        return 1;
    }


    private static int runShowPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();

        long points = serverLevel.getData(POINTS);
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(points)), true);
        return 1;
    }

    private static int runShowLandAmount(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();

        int land = serverLevel.getData(AMOUNT_OF_LAND);
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(land)), true);
        return 1;
    }

}