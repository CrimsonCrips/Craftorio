package org.crimsoncrips.craftorio.server.schematic;

import org.crimsoncrips.craftorio.skill_tree.UpgradeTree;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.schematic.ContractSchematicItem;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.networking.schematic.BuildBlitzPacket;
import org.crimsoncrips.craftorio.registries.contract.BuildPlacement;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;
import org.crimsoncrips.craftorio.server.data.CraftorioDataAttachments;
import org.crimsoncrips.craftorio.skill_tree.target.ModifierTarget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BuildBlitz {

    public static final ResourceLocation UPGRADE = Craftorio.prefix("build_blitz");

    private static final double MAX_RANGE = 64.0;
    private static final double TICKS_PER_LAUNCH = 7.5 / 1.6;
    private static final int MAX_LAUNCH_TICKS = 937;
    private static final int FLIGHT_TICKS = 20;
    private static final int PACKET_BATCH = 256;

    private static final class Volley {
        final UUID playerId;
        final UUID instance;
        final boolean creative;
        final long refundTicks;
        int remaining;
        int failed;

        Volley(UUID playerId, UUID instance, boolean creative, long refundTicks, int remaining) {
            this.playerId = playerId;
            this.instance = instance;
            this.creative = creative;
            this.refundTicks = refundTicks;
            this.remaining = remaining;
        }
    }

    private record Shot(Volley volley, ServerLevel level, BlockPos pos, BlockState state, List<SchematicStructure.Placed> partners,
                        ItemStack taken, long landsAt) {}

    private record Selection(SchematicStructure.Placed entry, List<SchematicStructure.Placed> partners, ItemStack taken) {}

    private static final List<Shot> PENDING = new ArrayList<>();

    private BuildBlitz() {}

    public static boolean isUnlocked(Player player) {
        return CraftorioMisc.hasUnlockedUpgrade(player, UpgradeTree.SACRIFICE, UPGRADE);
    }

    public static long cooldownTicks(Player player) {
        long base = (long) Craftorio.SERVER_CONFIG.BUILD_BLITZ_COOLDOWN_MINUTES.get() * 60L * CraftorioMisc.SECONDS_TO_TICKS;
        return Math.max(0L, Math.round(CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.BUILD_BLITZ_COOLDOWN, (double) base)));
    }

    public static double coveragePercent(Player player) {
        double percent = CraftorioMisc.applyUpgradeModifier(player, ModifierTarget.BUILD_BLITZ_COVERAGE, Craftorio.SERVER_CONFIG.BUILD_BLITZ_PERCENT.get());
        return Math.max(0.0, Math.min(100.0, percent));
    }

    public static boolean isActive(Player player) {
        for (Shot shot : PENDING) {
            if (shot.volley().playerId.equals(player.getUUID())) return true;
        }
        return false;
    }

    public static long ticksUntilReady(Player player) {
        return Math.max(0L, player.getData(CraftorioDataAttachments.BUILD_BLITZ_READY_AT) - player.level().getGameTime());
    }

    public static void fire(ServerPlayer player, SchematicData data) {
        if (!isUnlocked(player)) {
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_locked").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (data.origin().isEmpty()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_not_placed_yet").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (isActive(player)) {
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_active").withStyle(ChatFormatting.RED), true);
            return;
        }

        Optional<CraftorioContract> found = CraftorioSchematics.findContract(player, data.instance());
        if (found.isEmpty() || found.get().getProgress().submitted()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_inactive").withStyle(ChatFormatting.RED), true);
            return;
        }

        CraftorioContract contract = found.get();
        Optional<BuildPlacement> placement = contract.getProgress().placement();
        Optional<ResourceLocation> structureId = contract.getGoal().structure();
        if (placement.isEmpty() || structureId.isEmpty() || !placement.get().origin().equals(data.origin().get())) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_not_placed_yet").withStyle(ChatFormatting.RED), true);
            return;
        }

        Optional<SchematicStructure> structure = CraftorioSchematics.structure(player.server, structureId.get());
        ServerLevel level = player.server.getLevel(placement.get().origin().dimension());
        if (structure.isEmpty() || level == null) return;

        AABB bounds = structure.get().worldBounds(placement.get().origin().pos(), placement.get().rotation());
        if (level != player.level() || !bounds.inflate(MAX_RANGE).contains(player.position())) {
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_too_far").withStyle(ChatFormatting.RED), true);
            return;
        }

        boolean creative = player.isCreative();
        long remainingCooldown = ticksUntilReady(player);
        if (!creative && remainingCooldown > 0) {
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_cooldown",
                    CraftorioMisc.ticksToTimeString((int) Math.min(Integer.MAX_VALUE, remainingCooldown))).withStyle(ChatFormatting.RED), true);
            return;
        }

        List<SchematicStructure.Placed> placed = structure.get().placedIn(placement.get().origin().pos(), placement.get().rotation());
        Map<BlockPos, BlockState> expected = new HashMap<>();
        for (SchematicStructure.Placed entry : placed) {
            expected.put(entry.pos(), entry.state());
        }

        List<SchematicStructure.Placed> missing = new ArrayList<>();
        for (SchematicStructure.Placed entry : placed) {
            if (isSecondaryPart(entry.state()) || !level.isLoaded(entry.pos()) || !level.mayInteract(player, entry.pos())) continue;
            BlockState actual = level.getBlockState(entry.pos());
            if (SchematicStructure.matches(entry.state(), actual) || !isOpen(actual)) continue;
            missing.add(entry);
        }
        if (missing.isEmpty()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_nothing_missing").withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        int quota = (int) Math.ceil(missing.size() * coveragePercent(player) / 100.0);
        if (quota <= 0) return;

        Vec3 origin = player.getEyePosition().subtract(0.0, 0.4, 0.0);
        missing.sort(Comparator.<SchematicStructure.Placed>comparingInt(entry -> entry.pos().getY())
                .thenComparingDouble(entry -> entry.pos().getCenter().distanceToSqr(origin)));

        List<Selection> selections = new ArrayList<>();
        for (SchematicStructure.Placed entry : missing) {
            if (selections.size() >= quota) break;
            Item item = requiredItem(entry.state());
            if (creative) {
                selections.add(new Selection(entry, partnersOf(entry, expected), item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item)));
                continue;
            }
            if (item == Items.AIR) continue;

            ItemStack taken = takeOne(player.getInventory(), item);
            if (taken.isEmpty()) continue;
            selections.add(new Selection(entry, partnersOf(entry, expected), taken));
        }
        if (selections.isEmpty()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_no_materials").withStyle(ChatFormatting.RED), true);
            return;
        }

        long cooldown = creative ? 0L : cooldownTicks(player);
        long now = level.getGameTime();
        if (!creative) {
            player.setData(CraftorioDataAttachments.BUILD_BLITZ_READY_AT, now + cooldown);
        }

        Volley volley = new Volley(player.getUUID(), data.instance(), creative, cooldown / selections.size(), selections.size());
        int launchSpan = (int) Math.min(MAX_LAUNCH_TICKS, Math.round((selections.size() - 1) * TICKS_PER_LAUNCH));
        List<BuildBlitzPacket.Shot> packetShots = new ArrayList<>();
        for (int i = 0; i < selections.size(); i++) {
            Selection selection = selections.get(i);
            BlockPos pos = selection.entry().pos();
            int delay = selections.size() <= 1 ? 0 : (int) Math.round(i * launchSpan / (double) (selections.size() - 1));
            int flight = FLIGHT_TICKS;

            PENDING.add(new Shot(volley, level, pos, selection.entry().state(), selection.partners(), selection.taken(), now + delay + flight));
            packetShots.add(new BuildBlitzPacket.Shot(origin, pos, selection.entry().state(), selection.taken(), delay, flight));
            if (packetShots.size() >= PACKET_BATCH) {
                PacketDistributor.sendToPlayersInDimension(level, new BuildBlitzPacket(player.getId(), packetShots));
                packetShots = new ArrayList<>();
            }
        }
        if (!packetShots.isEmpty()) {
            PacketDistributor.sendToPlayersInDimension(level, new BuildBlitzPacket(player.getId(), packetShots));
        }

        level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_fired", selections.size(), missing.size()).withStyle(ChatFormatting.AQUA), true);
    }

    public static void tick(MinecraftServer server) {
        if (PENDING.isEmpty()) return;

        List<Shot> landing = new ArrayList<>();
        Iterator<Shot> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            Shot shot = iterator.next();
            if (shot.level().getServer() != server) {
                iterator.remove();
                continue;
            }
            if (shot.level().getGameTime() < shot.landsAt()) continue;
            iterator.remove();
            landing.add(shot);
        }
        for (Shot shot : landing) {
            land(server, shot);
        }
    }

    private static void land(MinecraftServer server, Shot shot) {
        ServerLevel level = shot.level();
        ServerPlayer player = server.getPlayerList().getPlayer(shot.volley().playerId);
        boolean active = player != null && CraftorioSchematics.findContract(player, shot.volley().instance)
                .map(contract -> !contract.getProgress().submitted()).orElse(false);

        BlockState actual = level.isLoaded(shot.pos()) ? level.getBlockState(shot.pos()) : null;
        boolean placeable = active && actual != null && isOpen(actual) && !SchematicStructure.matches(shot.state(), actual);

        if (placeable) {
            level.setBlock(shot.pos(), shot.state(), Block.UPDATE_ALL);
            for (SchematicStructure.Placed partner : shot.partners()) {
                if (isOpen(level.getBlockState(partner.pos()))) {
                    level.setBlock(partner.pos(), partner.state(), Block.UPDATE_ALL);
                }
            }


            if (!shot.volley().creative && player != null && shot.taken().getItem() instanceof BucketItem && shot.taken().getItem() != Items.BUCKET) {
                giveBack(player, new ItemStack(Items.BUCKET), level, shot.pos());
            }
        } else {
            shot.volley().failed++;
            if (!shot.volley().creative) {
                giveBack(player, shot.taken(), level, shot.pos());
                if (player != null) {
                    long readyAt = player.getData(CraftorioDataAttachments.BUILD_BLITZ_READY_AT);
                    player.setData(CraftorioDataAttachments.BUILD_BLITZ_READY_AT,
                            Math.max(level.getGameTime(), readyAt - shot.volley().refundTicks));
                }
            }
        }

        if (--shot.volley().remaining <= 0 && shot.volley().failed > 0 && player != null) {
            long refunded = shot.volley().creative ? 0L : shot.volley().refundTicks * shot.volley().failed;
            player.displayClientMessage(Component.translatable("misc.craftorio.build_blitz_returned", shot.volley().failed,
                    CraftorioMisc.ticksToTimeString((int) Math.min(Integer.MAX_VALUE, refunded))).withStyle(ChatFormatting.YELLOW), false);
        }
    }

    private static void giveBack(ServerPlayer player, ItemStack stack, ServerLevel level, BlockPos fallback) {
        if (stack.isEmpty()) return;
        if (player != null) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            return;
        }
        level.addFreshEntity(new ItemEntity(level, fallback.getX() + 0.5, fallback.getY() + 0.5, fallback.getZ() + 0.5, stack));
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static boolean isSecondaryPart(BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) return true;
        return state.hasProperty(BlockStateProperties.BED_PART) && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD;
    }

    private static List<SchematicStructure.Placed> partnersOf(SchematicStructure.Placed entry, Map<BlockPos, BlockState> expected) {
        BlockState state = entry.state();
        BlockPos partnerPos = null;
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            partnerPos = entry.pos().above();
        } else if (state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            partnerPos = entry.pos().relative(facing);
        }
        if (partnerPos == null) return List.of();

        BlockState partner = expected.get(partnerPos);
        if (partner == null || !partner.is(state.getBlock())) return List.of();
        return List.of(new SchematicStructure.Placed(partnerPos, partner));
    }

    public static Item requiredItem(BlockState state) {
        if (state.getBlock() instanceof LiquidBlock liquid) {
            return liquid.fluid.getBucket();
        }
        return state.getBlock().asItem();
    }

    private static ItemStack takeOne(Inventory inventory, Item item) {
        List<ItemStack> candidates = new ArrayList<>(inventory.items);
        candidates.addAll(inventory.offhand);
        ItemStack fallback = null;
        for (ItemStack stack : candidates) {
            if (stack.isEmpty() || !stack.is(item) || stack.getItem() instanceof ContractSchematicItem) continue;
            if (stack.getComponentsPatch().isEmpty()) {
                return stack.split(1);
            }
            if (fallback == null) fallback = stack;
        }
        return fallback == null ? ItemStack.EMPTY : fallback.split(1);
    }
}
