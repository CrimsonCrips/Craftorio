package org.crimsoncrips.craftorio.server.schematic;

import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.crimsoncrips.craftorio.networking.schematic.ChronospherePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.CraftorioMisc;
import org.crimsoncrips.craftorio.item.CraftorioItems;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.registries.contract.BuildPlacement;
import org.crimsoncrips.craftorio.registries.contract.ContractType;
import org.crimsoncrips.craftorio.registries.contract.CraftorioContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class CraftorioSchematics {

    private static final int CHECK_INTERVAL_TICKS = 20;

    private static final class PendingRemoval {
        final UUID instance;
        final ServerLevel level;
        final Consumer<MinecraftServer> finish;
        int ticksLeft;

        PendingRemoval(UUID instance, ServerLevel level, int ticksLeft, Consumer<MinecraftServer> finish) {
            this.instance = instance;
            this.level = level;
            this.ticksLeft = ticksLeft;
            this.finish = finish;
        }
    }

    private static final List<PendingRemoval> PENDING = new ArrayList<>();
    private static final Map<ResourceLocation, Optional<CompoundTag>> TAGS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Optional<SchematicStructure>> STRUCTURES = new ConcurrentHashMap<>();

    private CraftorioSchematics() {}

    public static void clearCache() {
        TAGS.clear();
        STRUCTURES.clear();
    }

    public static Optional<CompoundTag> tag(MinecraftServer server, ResourceLocation id) {
        return TAGS.computeIfAbsent(id, key -> {
            try {
                return server.getStructureManager().get(key).map(template -> template.save(new CompoundTag()));
            } catch (RuntimeException e) {
                Craftorio.LOGGER.error("Failed to load the schematic structure {}", key, e);
                return Optional.empty();
            }
        });
    }

    public static Optional<SchematicStructure> structure(MinecraftServer server, ResourceLocation id) {
        return STRUCTURES.computeIfAbsent(id, key -> tag(server, key).map(tag -> SchematicStructure.parse(tag, BuiltInRegistries.BLOCK.asLookup())));
    }

    public static void prepareContract(CraftorioContract contract) {
        if (contract.getType() != ContractType.BUILDING) return;
        contract.setProgress(contract.getProgress().withInstance(UUID.randomUUID()));
    }

    public static void giveSchematic(ServerPlayer player, CraftorioContract contract, ResourceLocation contractId) {
        if (contract.getType() != ContractType.BUILDING) return;

        Optional<ResourceLocation> structureId = contract.getGoal().structure();
        Optional<UUID> instance = contract.getProgress().instance();
        if (structureId.isEmpty() || instance.isEmpty()) return;

        Optional<SchematicStructure> structure = structure(player.server, structureId.get());
        if (structure.isEmpty()) {
            player.sendSystemMessage(Component.translatable("misc.craftorio.schematic_missing_structure", structureId.get().toString()).withStyle(ChatFormatting.RED));
        }
        contract.setProgress(contract.getProgress().withBuild(0, structure.map(value -> value.entries().size()).orElse(0)));

        ItemStack stack = new ItemStack(CraftorioItems.SCHEMATIC.get());
        String owner = CraftorioMisc.universalBased(player.level()) ? "" : player.getGameProfile().getName();
        stack.set(CraftorioDataComponents.SCHEMATIC.get(), new SchematicData(instance.get(), contractId, structureId.get(),
                owner, Rotation.NONE, Optional.empty(), false));
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public static void giveSchematicCopy(ServerPlayer player, UUID instance) {
        Optional<CraftorioContract> found = findContract(player, instance);
        if (found.isEmpty()) return;

        CraftorioContract contract = found.get();
        Optional<ResourceLocation> structureId = contract.getGoal().structure();
        if (contract.getType() != ContractType.BUILDING || structureId.isEmpty() || contract.getProgress().submitted()) return;

        ResourceLocation contractId = player.registryAccess().registryOrThrow(CraftorioContract.REGISTRY_KEY).entrySet().stream()
                .filter(entry -> entry.getValue().getName().equals(contract.getName())
                        && entry.getValue().getGoal().structure().equals(structureId))
                .map(entry -> entry.getKey().location())
                .findFirst()
                .orElse(structureId.get());

        ItemStack stack = new ItemStack(CraftorioItems.SCHEMATIC.get());
        String owner = CraftorioMisc.universalBased(player.level()) ? "" : player.getGameProfile().getName();
        stack.set(CraftorioDataComponents.SCHEMATIC.get(), new SchematicData(instance, contractId, structureId.get(),
                owner, Rotation.NONE, Optional.empty(), false));
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        player.displayClientMessage(Component.translatable("misc.craftorio.schematic_copy_given").withStyle(ChatFormatting.AQUA), true);
    }

    public static Optional<CraftorioContract> findContract(Player player, UUID instance) {
        for (CraftorioContract contract : CraftorioMisc.getCraftorioContracts(player)) {
            if (contract.getProgress().instance().filter(instance::equals).isPresent()) {
                return Optional.of(contract);
            }
        }
        return Optional.empty();
    }

    public static boolean isLinked(Player player, SchematicData data) {
        return findContract(player, data.instance()).isPresent();
    }

    public static boolean isLinked(MinecraftServer server, SchematicData data) {
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (isLinked(online, data)) return true;
        }
        return false;
    }

    public static void place(ServerPlayer player, ItemStack stack, SchematicData data, Optional<GlobalPos> origin, Rotation rotation) {
        stack.set(CraftorioDataComponents.SCHEMATIC.get(), data.withOrigin(origin).withRotation(rotation));

        Optional<CraftorioContract> contract = findContract(player, data.instance());
        if (contract.isEmpty()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_inactive").withStyle(ChatFormatting.RED), true);
            return;
        }

        Optional<BuildPlacement> placement = origin.map(pos -> new BuildPlacement(pos, rotation));
        CraftorioContract active = contract.get();
        active.setProgress(active.getProgress().withPlacement(placement));
        updateProgress(player.server, active);
        CraftorioMisc.refreshContracts(player);
    }

    public static void pickUp(ServerPlayer player, ItemStack stack, SchematicData data) {
        place(player, stack, data, Optional.empty(), data.rotation());
        player.displayClientMessage(Component.translatable("misc.craftorio.schematic_picked_up").withStyle(ChatFormatting.AQUA), true);
    }

    public static void submit(ServerPlayer player, ItemStack stack, SchematicData data) {
        Optional<CraftorioContract> found = findContract(player, data.instance());
        if (found.isEmpty()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_inactive").withStyle(ChatFormatting.RED), true);
            return;
        }

        CraftorioContract contract = found.get();
        Optional<BuildPlacement> placement = contract.getProgress().placement();
        Optional<ResourceLocation> structureId = contract.getGoal().structure();
        if (contract.getProgress().submitted() || structureId.isEmpty() || isRemovalPending(data.instance())) return;
        if (placement.isEmpty()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_not_placed_yet").withStyle(ChatFormatting.RED), true);
            return;
        }

        Optional<SchematicStructure> structure = structure(player.server, structureId.get());
        ServerLevel level = player.server.getLevel(placement.get().origin().dimension());
        if (structure.isEmpty() || level == null) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_missing_structure", structureId.get().toString()).withStyle(ChatFormatting.RED), true);
            return;
        }

        SchematicStructure.Check check = structure.get().check(level, placement.get());
        if (check.placed() < check.total()) {
            player.displayClientMessage(Component.translatable("misc.craftorio.schematic_submit_incomplete", check.total() - check.placed(), check.wrong()).withStyle(ChatFormatting.RED), true);
            return;
        }

        contract.setProgress(contract.getProgress().withBuild(check.total(), check.total()).withSubmitted(true));
        CraftorioMisc.refreshContracts(player);
        beginRemoval(level, structure.get(), placement.get(), data.instance());
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("misc.craftorio.schematic_submit_success").withStyle(ChatFormatting.GREEN), true);
    }

    public static void instaComplete(ServerPlayer player, SchematicData data) {
        if (!player.isCreative()) return;

        Optional<CraftorioContract> found = findContract(player, data.instance());
        if (found.isEmpty()) return;

        CraftorioContract contract = found.get();
        Optional<BuildPlacement> placement = contract.getProgress().placement();
        Optional<ResourceLocation> structureId = contract.getGoal().structure();
        if (contract.getProgress().submitted() || structureId.isEmpty() || placement.isEmpty() || isRemovalPending(data.instance())) return;

        Optional<SchematicStructure> structure = structure(player.server, structureId.get());
        ServerLevel level = player.server.getLevel(placement.get().origin().dimension());
        if (structure.isEmpty() || level == null) return;

        placeWithoutUpdates(level, structure.get().placedIn(placement.get().origin().pos(), placement.get().rotation()));

        updateProgress(player.server, contract);
        player.displayClientMessage(Component.translatable("misc.craftorio.schematic_insta_complete_success").withStyle(ChatFormatting.AQUA), true);
        CraftorioMisc.refreshContracts(player);
    }

    private record Placement(BlockPos pos, BlockState previous, BlockState placed) {}

    private static void placeWithoutUpdates(ServerLevel level, List<SchematicStructure.Placed> entries) {
        List<Placement> changed = new ArrayList<>();
        boolean wasCapturing = level.captureBlockSnapshots;
        int capturedBefore = level.capturedBlockSnapshots.size();
        level.captureBlockSnapshots = true;
        try {
            for (SchematicStructure.Placed entry : entries) {
                BlockState previous = level.getBlockState(entry.pos());
                if (previous == entry.state()) continue;
                if (level.setBlock(entry.pos(), entry.state(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) {
                    changed.add(new Placement(entry.pos(), previous, entry.state()));
                }
            }
        } finally {
            level.captureBlockSnapshots = wasCapturing;
            level.capturedBlockSnapshots.subList(capturedBefore, level.capturedBlockSnapshots.size()).clear();
        }

        for (Placement placement : changed) {
            level.sendBlockUpdated(placement.pos(), placement.previous(), placement.placed(), Block.UPDATE_CLIENTS);
            level.onBlockStateChange(placement.pos(), placement.previous(), placement.placed());
        }
    }

    private static boolean isRemovalPending(UUID instance) {
        for (PendingRemoval pending : PENDING) {
            if (pending.instance != null && pending.instance.equals(instance)) return true;
        }
        return false;
    }

    private static void beginRemoval(ServerLevel level, SchematicStructure structure, BuildPlacement placement, UUID instance) {
        AABB bounds = structure.worldBounds(placement.origin().pos(), placement.rotation());
        List<BlockPos> positions = new ArrayList<>();
        for (SchematicStructure.Placed entry : structure.placedIn(placement.origin().pos(), placement.rotation())) {
            positions.add(entry.pos());
        }
        int removalTicks = ChronosphereRemoval.removalTicks(positions.size());
        PacketDistributor.sendToPlayersInDimension(level, ChronospherePacket.around(bounds, removalTicks));
        PENDING.add(new PendingRemoval(instance, level, ChronospherePacket.APPEAR_TICKS + ChronospherePacket.HOLD_TICKS,
                server -> ChronosphereRemoval.start(level, bounds, positions, removalTicks)));
    }

    public static void removeArea(ServerLevel level, BlockPos min, BlockPos max) {
        AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        int solid = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.getBlockState(pos).isAir()) solid++;
        }
        int removalTicks = ChronosphereRemoval.removalTicks(solid);
        PacketDistributor.sendToPlayersInDimension(level, ChronospherePacket.around(bounds, removalTicks));
        PENDING.add(new PendingRemoval(null, level, ChronospherePacket.APPEAR_TICKS + ChronospherePacket.HOLD_TICKS,
                server -> ChronosphereRemoval.start(level, bounds, BlockPos.betweenClosed(min, max), removalTicks)));
    }

    private static void tickRemovals(MinecraftServer server) {
        Iterator<PendingRemoval> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            PendingRemoval pending = iterator.next();
            if (pending.level.getServer() != server) {
                iterator.remove();
                continue;
            }
            if (--pending.ticksLeft > 0) continue;

            iterator.remove();
            pending.finish.accept(server);
        }
    }

    private static boolean updateProgress(MinecraftServer server, CraftorioContract contract) {
        if (contract.getProgress().submitted()) return false;

        Optional<BuildPlacement> placement = contract.getProgress().placement();
        Optional<ResourceLocation> structureId = contract.getGoal().structure();
        if (placement.isEmpty() || structureId.isEmpty()) return false;

        ServerLevel level = server.getLevel(placement.get().origin().dimension());
        Optional<SchematicStructure> structure = structure(server, structureId.get());
        if (level == null || structure.isEmpty()) return false;

        int total = structure.get().entries().size();
        int placed = structure.get().countPlaced(level, placement.get());
        if (placed == contract.getProgress().blocksPlaced() && total == contract.getProgress().blocksTotal()) return false;

        contract.setProgress(contract.getProgress().withBuild(placed, total));
        return true;
    }

    public static void tick(MinecraftServer server) {
        tickRemovals(server);
        ChronosphereRemoval.tick(server);
        BuildBlitz.tick(server);
        if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) return;

        Set<List<CraftorioContract>> checked = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            List<CraftorioContract> contracts = CraftorioMisc.getCraftorioContracts(player);
            if (!checked.add(contracts)) continue;

            boolean changed = false;
            for (CraftorioContract contract : new ArrayList<>(contracts)) {
                if (contract.getType() == ContractType.BUILDING && !contract.isAbandoned()) {
                    changed |= updateProgress(server, contract);
                }
            }
            if (changed) {
                CraftorioMisc.refreshContracts(player);
            }
        }
    }

    public static void onContractEnded(Player player, CraftorioContract contract) {
        if (player.level().isClientSide() || contract.getType() != ContractType.BUILDING) return;
        contract.getProgress().instance().ifPresent(instance -> {
            List<? extends Player> holders = CraftorioMisc.universalBased(CraftorioMisc.universalLevel(player)) && player.getServer() != null
                    ? player.getServer().getPlayerList().getPlayers() : List.of(player);
            for (Player holder : holders) {
                removeSchematics(holder, instance);
            }
        });
    }

    private static void removeSchematics(Player player, UUID instance) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isSchematicFor(inventory.getItem(slot), instance)) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        if (isSchematicFor(player.containerMenu.getCarried(), instance)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
    }

    private static boolean isSchematicFor(ItemStack stack, UUID instance) {
        SchematicData data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
        return data != null && data.instance().equals(instance);
    }
}
