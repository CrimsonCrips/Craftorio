package org.crimsoncrips.craftorio.server.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.networking.ExpandScreenPacket;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.crimsoncrips.craftorio.CraftorioMisc.pointThreshold;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.AMOUNT_OF_LAND;
import static org.crimsoncrips.craftorio.server.CraftorioDataAttachments.POINTS;


public class CommandEvents {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("craftorio").then(
                        Commands.literal("points")
                                .then(Commands.literal("valueless_items").requires(cs -> cs.hasPermission(2)).executes(CommandEvents::runValueless))
                                .then(Commands.literal("add").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runAddPoints)))
                                .then(Commands.literal("set").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runSetPoints)))
                                .then(Commands.literal("subtract").requires(cs -> cs.hasPermission(3)).then(Commands.argument("amount",StringArgumentType.string()).executes(CommandEvents::runSubtractPoints)))).then(
                        Commands.literal("land")
                                .then(Commands.literal("check_amount").requires(cs -> cs.hasPermission(2)).executes(CommandEvents::runShowLandAmount))
                                .then(Commands.literal("border_expand").executes(CommandEvents::runBorderExpand))).then(

                        Commands.literal("point_value")
                                .then(Commands.literal("highest_value").then(Commands.argument("list_no", IntegerArgumentType.integer()).executes(CommandEvents::runHighestValue)))

                ).then(Commands.literal("check_values").requires(cs -> cs.hasPermission(2)).executes(CommandEvents::runPropertiesCheck))

        );


    }

    private static int runPropertiesCheck(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        if (serverPlayer != null) {
            context.getSource().sendSuccess(() -> Component.literal("No Borders:" + CraftorioMisc.isNoBorders(serverPlayer.level())), true);
            context.getSource().sendSuccess(() -> Component.literal("Universal:" + CraftorioMisc.universalBased(serverPlayer.level())), true);
            context.getSource().sendSuccess(() -> Component.literal("ChunkBased:" + CraftorioMisc.chunkBased(serverPlayer.level())), true);

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

    private static int runHighestValue(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        List<ItemStack> listOfItems = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            listOfItems.add(item.getDefaultInstance());
        });
        int limit = IntegerArgumentType.getInteger(context,"list_no");

        List<ItemStack> highestValueItems = listOfItems.stream()
                .sorted(Comparator.comparing((ItemStack stack) -> CraftorioMisc.checkValue(stack, player,false)).reversed())
                .limit(limit)
                .toList();

        for (ItemStack itemStack : highestValueItems){
            BigInteger pointsValue = CraftorioMisc.checkValue(itemStack, player,false);
            String string = itemStack.getDisplayName().getString() + " " + pointsValue;
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        return 1;
    }

    private static int runValueless(CommandContext<CommandSourceStack> context) {
        BuiltInRegistries.ITEM.forEach(item -> {
            BigInteger points = CraftorioMisc.checkValue(new ItemStack(item),context.getSource().getPlayer(),false);
            if (points.compareTo(BigInteger.ZERO) <= 0){
                context.getSource().sendSuccess(() -> Component.literal(item.getDefaultInstance().getDisplayName().getString() + " " + points), true);
            }
        });
        return 1;
    }

    private static int runSetPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        BigInteger points = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));

        if (points.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        CraftorioMisc.setPoints(serverLevel,points,context.getSource().getPlayer());
        return 1;
    }

    private static int runAddPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        BigInteger points = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));
        points = points.add(CraftorioMisc.getPoints(serverLevel,context.getSource().getPlayer()));

        if (points.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        CraftorioMisc.setPoints(serverLevel,points,context.getSource().getPlayer());
        return 1;
    }

    private static int runSubtractPoints(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        BigInteger points = CraftorioMisc.toBigInteger(StringArgumentType.getString(context,"amount"));
        points = points.subtract(CraftorioMisc.getPoints(serverLevel,context.getSource().getPlayer()));

        if (points.compareTo(pointThreshold()) >= 0) {
            String string = Component.translatable("misc.craftorio.too_much_value").getString();
            context.getSource().sendSuccess(() -> Component.literal(string), true);
        }
        CraftorioMisc.setPoints(serverLevel,points,context.getSource().getPlayer());
        return 1;
    }

    private static int runShowLandAmount(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();

        long land = CraftorioMisc.getLandAmount(serverLevel,context.getSource().getPlayer());
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(land)), true);
        return 1;
    }

}