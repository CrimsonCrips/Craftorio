package org.crimsoncrips.craftorio.server.schematic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.crimsoncrips.craftorio.registries.contract.BuildPlacement;

import java.util.ArrayList;
import java.util.List;

public final class SchematicStructure {

    public record Entry(BlockPos pos, BlockState state) {}

    public record Placed(BlockPos pos, BlockState state) {}

    private final Vec3i size;
    private final List<Entry> entries;
    private final List<BlockPos> air;

    private SchematicStructure(Vec3i size, List<Entry> entries, List<BlockPos> air) {
        this.size = size;
        this.entries = entries;
        this.air = air;
    }

    public Vec3i size() {
        return size;
    }

    public List<Entry> entries() {
        return entries;
    }

    public static SchematicStructure parse(CompoundTag tag, HolderLookup<Block> blocks) {
        ListTag sizeTag = tag.getList("size", Tag.TAG_INT);
        Vec3i size = sizeTag.size() == 3 ? new Vec3i(sizeTag.getInt(0), sizeTag.getInt(1), sizeTag.getInt(2)) : Vec3i.ZERO;

        ListTag paletteTag = tag.contains("palettes", Tag.TAG_LIST) && !tag.getList("palettes", Tag.TAG_LIST).isEmpty()
                ? tag.getList("palettes", Tag.TAG_LIST).getList(0)
                : tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>(paletteTag.size());
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(blocks, paletteTag.getCompound(i)));
        }

        List<Entry> entries = new ArrayList<>();
        List<BlockPos> air = new ArrayList<>();
        ListTag blockList = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blockList.size(); i++) {
            CompoundTag blockTag = blockList.getCompound(i);
            int stateIndex = blockTag.getInt("state");
            if (stateIndex < 0 || stateIndex >= palette.size()) continue;

            BlockState state = palette.get(stateIndex);
            if (state.is(Blocks.JIGSAW)) {
                state = jigsawFinalState(blockTag, blocks);
            }
            if (state == null || state.is(Blocks.STRUCTURE_VOID) || state.is(Blocks.STRUCTURE_BLOCK)) continue;

            ListTag posTag = blockTag.getList("pos", Tag.TAG_INT);
            if (posTag.size() != 3) continue;
            BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
            if (state.isAir()) {
                air.add(pos);
            } else {
                entries.add(new Entry(pos, state));
            }
        }
        return new SchematicStructure(size, List.copyOf(entries), List.copyOf(air));
    }

    private static BlockState jigsawFinalState(CompoundTag blockTag, HolderLookup<Block> blocks) {
        if (!blockTag.contains("nbt", Tag.TAG_COMPOUND)) return null;

        String finalState = blockTag.getCompound("nbt").getString("final_state");
        if (finalState.isEmpty()) return null;
        try {
            return BlockStateParser.parseForBlock(blocks, finalState, false).blockState();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    public AABB worldBounds(BlockPos origin, Rotation rotation) {
        List<Placed> placed = placedIn(origin, rotation);
        if (placed.isEmpty()) {
            return new AABB(origin);
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Placed entry : placed) {
            BlockPos pos = entry.pos();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public record Check(int placed, int total, int wrong) {}

    public Check check(Level level, BuildPlacement placement) {
        int placed = 0;
        int wrong = 0;
        List<Placed> all = placedIn(placement.origin().pos(), placement.rotation());
        for (Placed entry : all) {
            if (!level.isLoaded(entry.pos())) continue;

            BlockState actual = level.getBlockState(entry.pos());
            if (matches(entry.state(), actual)) {
                placed++;
            } else if (!actual.isAir() && !actual.canBeReplaced()) {
                wrong++;
            }
        }
        return new Check(placed, all.size(), wrong);
    }

    public List<Placed> placedIn(BlockPos origin, Rotation rotation) {
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
        List<Placed> placed = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            BlockPos world = origin.offset(StructureTemplate.calculateRelativePosition(settings, entry.pos()));
            placed.add(new Placed(world, entry.state().rotate(rotation)));
        }
        return placed;
    }

    public List<BlockPos> airIn(BlockPos origin, Rotation rotation) {
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
        List<BlockPos> placed = new ArrayList<>(air.size());
        for (BlockPos pos : air) {
            placed.add(origin.offset(StructureTemplate.calculateRelativePosition(settings, pos)));
        }
        return placed;
    }

    public static boolean matches(BlockState expected, BlockState actual) {
        return actual.is(expected.getBlock());
    }

    public int countPlaced(Level level, BuildPlacement placement) {
        int count = 0;
        for (Placed placed : placedIn(placement.origin().pos(), placement.rotation())) {
            if (level.isLoaded(placed.pos()) && matches(placed.state(), level.getBlockState(placed.pos()))) {
                count++;
            }
        }
        return count;
    }
}
