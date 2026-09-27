package org.crimsoncrips.craftorio.item.schematic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.fml.loading.FMLEnvironment;
import org.crimsoncrips.craftorio.client.schematic.ClientPlayerAccess;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;

import java.util.List;
import java.util.Optional;

public class ContractSchematicItem extends Item {

    public ContractSchematicItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        SchematicData data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
        if (player == null || data == null || !CraftorioSchematics.isLinked(player, data)) return InteractionResult.PASS;
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (player.isShiftKeyDown()) {
            rotate(serverPlayer, stack, data);
        } else if (data.origin().isPresent()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_already_placed", Component.keybind("key.craftorio.print_scan")).withStyle(ChatFormatting.YELLOW), true);
        } else {
            GlobalPos origin = GlobalPos.of(context.getLevel().dimension(), context.getClickedPos().relative(context.getClickedFace()));
            CraftorioSchematics.place(serverPlayer, stack, data, Optional.of(origin), data.rotation());
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_placed", origin.pos().toShortString()).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SchematicData data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
        if (data == null || !player.isShiftKeyDown() || !CraftorioSchematics.isLinked(player, data)) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide()) {
            rotate((ServerPlayer) player, stack, data);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void rotate(ServerPlayer player, ItemStack stack, SchematicData data) {
        Rotation rotation = data.rotation().getRotated(Rotation.CLOCKWISE_90);
        CraftorioSchematics.place(player, stack, data, data.origin(), rotation);
        player.displayClientMessage(Component.translatable("misc.craftorio.schematic_rotated", rotation.ordinal() * 90).withStyle(ChatFormatting.AQUA), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        SchematicData data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
        if (data == null) return;

        Player player = FMLEnvironment.dist.isClient() ? ClientPlayerAccess.player() : null;
        if (player != null && !CraftorioSchematics.isLinked(player, data)) {
            tooltip.add(Component.translatable("misc.craftorio.schematic_inactive").withStyle(ChatFormatting.RED));
            return;
        }

        tooltip.add(Component.translatable("misc.craftorio.schematic_owner", data.owner().isEmpty() ? Component.translatable("misc.craftorio.schematic_owner_any") : Component.literal(data.owner())).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("misc.craftorio.schematic_structure", data.structure().toString()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("misc.craftorio.schematic_rotation", data.rotation().ordinal() * 90).withStyle(ChatFormatting.GRAY));
        data.origin().ifPresentOrElse(
                origin -> tooltip.add(Component.translatable("misc.craftorio.schematic_origin", origin.pos().toShortString(), origin.dimension().location().toString()).withStyle(ChatFormatting.GRAY)),
                () -> tooltip.add(Component.translatable("misc.craftorio.schematic_not_placed").withStyle(ChatFormatting.YELLOW)));

        if (player != null) {
            CraftorioSchematics.findContract(player, data.instance()).ifPresent(active -> {
                if (active.getProgress().submitted()) {
                    tooltip.add(Component.translatable("misc.craftorio.schematic_settled").withStyle(ChatFormatting.GREEN));
                } else {
                    tooltip.add(Component.translatable("misc.craftorio.schematic_progress", active.getProgress().blocksPlaced(), active.getProgress().blocksTotal()).withStyle(ChatFormatting.GOLD));
                }
            });
        }
        if (data.converted()) {
            tooltip.add(Component.translatable("misc.craftorio.schematic_converted").withStyle(ChatFormatting.DARK_AQUA));
        }
        tooltip.add(Component.translatable("misc.craftorio.schematic_help_place").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("misc.craftorio.schematic_help_rotate").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("misc.craftorio.schematic_help_menu", Component.keybind("key.craftorio.print_scan")).withStyle(ChatFormatting.DARK_GRAY));
    }
}
