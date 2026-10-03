package org.crimsoncrips.craftorio.client.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.server.schematic.SchematicStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class SchematicVerifier {

    public enum Kind {
        MISSING(0x55AAFF),
        WRONG_BLOCK(0xFF5555),
        WRONG_STATE(0xFFAA00),
        EXTRA(0xFF55FF);

        private final int color;

        Kind(int color) {
            this.color = color;
        }

        public int color() {
            return color;
        }
    }

    public record Issue(Kind kind, BlockPos pos, BlockState expected, BlockState actual) {}

    public record Result(List<SchematicStructure.Placed> ghost, List<Issue> issues, Map<BlockPos, Issue> byPos,
                         int placed, int total, AABB bounds, long ghostHash) {

        public int count(Kind kind) {
            int count = 0;
            for (Issue issue : issues) {
                if (issue.kind() == kind) count++;
            }
            return count;
        }
    }

    private static final Set<String> ORIENTATION_NAMES = Set.of("half", "rotation", "face", "hinge", "attachment", "orientation");

    private SchematicVerifier() {}

    public static Result analyze(Level level, SchematicStructure structure, BlockPos origin, Rotation rotation) {
        List<SchematicStructure.Placed> placed = structure.placedIn(origin, rotation);
        List<SchematicStructure.Placed> ghost = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();
        Map<BlockPos, Issue> byPos = new HashMap<>();
        long hash = 17L;
        int correct = 0;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (SchematicStructure.Placed entry : placed) {
            BlockPos pos = entry.pos();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());

            BlockState actual = level.getBlockState(pos);
            if (SchematicStructure.matches(entry.state(), actual)) {
                correct++;
                if (!stateDifferences(entry.state(), actual).isEmpty()) {
                    add(issues, byPos, new Issue(Kind.WRONG_STATE, pos, entry.state(), actual));
                }
                continue;
            }

            ghost.add(entry);
            hash = hash * 31L + pos.asLong();
            boolean occupied = !actual.isAir() && !actual.canBeReplaced();
            add(issues, byPos, new Issue(occupied ? Kind.WRONG_BLOCK : Kind.MISSING, pos, entry.state(), actual));
        }

        for (BlockPos pos : structure.airIn(origin, rotation)) {
            if (!level.isLoaded(pos)) continue;
            BlockState actual = level.getBlockState(pos);
            if (actual.isAir()) continue;
            if (actual.canBeReplaced() && actual.getFluidState().isEmpty()) continue;
            add(issues, byPos, new Issue(Kind.EXTRA, pos, null, actual));
        }

        AABB bounds = placed.isEmpty() ? null : new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        return new Result(ghost, issues, byPos, correct, placed.size(), bounds, hash);
    }

    private static void add(List<Issue> issues, Map<BlockPos, Issue> byPos, Issue issue) {
        issues.add(issue);
        byPos.put(issue.pos(), issue);
    }

    public static List<Property<?>> stateDifferences(BlockState expected, BlockState actual) {
        List<Property<?>> differences = new ArrayList<>();
        for (Property<?> property : expected.getProperties()) {
            if (!actual.hasProperty(property) || !isRelevant(expected, property)) continue;
            if (!expected.getValue(property).equals(actual.getValue(property))) {
                differences.add(property);
            }
        }
        return differences;
    }

    private static boolean isRelevant(BlockState expected, Property<?> property) {
        Class<?> valueClass = property.getValueClass();
        if (valueClass == Direction.class || valueClass == Direction.Axis.class || valueClass == FrontAndTop.class) return true;
        if (property == BlockStateProperties.SLAB_TYPE) return true;
        if (property == LiquidBlock.LEVEL) return expected.getBlock() instanceof LiquidBlock;
        return ORIENTATION_NAMES.contains(property.getName());
    }

    public static Component describe(Issue issue) {
        return switch (issue.kind()) {
            case MISSING -> Component.translatable("misc.craftorio.schematic_issue_missing", issue.expected().getBlock().getName());
            case WRONG_BLOCK -> Component.translatable("misc.craftorio.schematic_issue_wrong_block", issue.expected().getBlock().getName(), issue.actual().getBlock().getName());
            case EXTRA -> Component.translatable("misc.craftorio.schematic_issue_extra", issue.actual().getBlock().getName());
            case WRONG_STATE -> describeState(issue);
        };
    }

    private static Component describeState(Issue issue) {
        List<Property<?>> differences = stateDifferences(issue.expected(), issue.actual());
        if (issue.expected().getBlock() instanceof LiquidBlock && differences.contains(LiquidBlock.LEVEL)) {
            return Component.translatable("misc.craftorio.schematic_issue_fluid_source", issue.expected().getBlock().getName());
        }

        MutableComponent details = Component.empty();
        for (int i = 0; i < differences.size(); i++) {
            Property<?> property = differences.get(i);
            if (i > 0) details.append(", ");
            details.append(Component.translatable("misc.craftorio.schematic_issue_state_change", property.getName(),
                    valueName(issue.actual(), property), valueName(issue.expected(), property)));
        }
        return Component.translatable("misc.craftorio.schematic_issue_wrong_state", issue.expected().getBlock().getName(), details);
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
