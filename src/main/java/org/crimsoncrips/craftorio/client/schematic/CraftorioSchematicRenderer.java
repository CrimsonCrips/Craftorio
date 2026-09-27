package org.crimsoncrips.craftorio.client.schematic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.crimsoncrips.craftorio.Craftorio;
import org.crimsoncrips.craftorio.item.schematic.SchematicData;
import org.crimsoncrips.craftorio.registries.CraftorioDataComponents;
import org.crimsoncrips.craftorio.server.schematic.SchematicStructure;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class CraftorioSchematicRenderer {

    private static final float GHOST_ALPHA = 0.45f;
    private static final int REFRESH_TICKS = 10;

    private record Key(ResourceLocation structure, BlockPos origin, Rotation rotation) {}

    private static final class Ghost {
        VertexBuffer buffer;
        long missingHash = Long.MIN_VALUE;
        List<BlockPos> wrong = List.of();
        List<BlockPos> unrenderable = List.of();
        AABB bounds;
        int placed;
        int total;
        long lastRefresh = Long.MIN_VALUE;

        void close() {
            if (buffer != null) {
                buffer.close();
                buffer = null;
            }
        }
    }

    private record GhostConsumer(VertexConsumer delegate) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, Math.round(alpha * GHOST_ALPHA));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }
    }

    private static final Map<Key, Ghost> GHOSTS = new HashMap<>();
    private static final Map<Key, Ghost> PREVIEWS = new HashMap<>();

    private CraftorioSchematicRenderer() {}

    public static void invalidate(ResourceLocation structure) {
        GHOSTS.entrySet().removeIf(entry -> {
            if (!entry.getKey().structure().equals(structure)) return false;
            entry.getValue().close();
            return true;
        });
        PREVIEWS.entrySet().removeIf(entry -> {
            if (!entry.getKey().structure().equals(structure)) return false;
            entry.getValue().close();
            return true;
        });
    }

    public static void clear() {
        GHOSTS.values().forEach(Ghost::close);
        GHOSTS.clear();
        PREVIEWS.values().forEach(Ghost::close);
        PREVIEWS.clear();
    }

    private static SchematicData heldData(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            SchematicData data = player.getItemInHand(hand).get(CraftorioDataComponents.SCHEMATIC.get());
            if (data != null) return data;
        }
        return null;
    }

    private static void renderPreview(RenderLevelStageEvent event, Minecraft minecraft, ClientLevel level, SchematicData data, Vec3 camera) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        Optional<SchematicStructure> structure = ClientSchematics.get(data.structure());
        if (structure.isEmpty()) return;

        BlockPos origin = hit.getBlockPos().relative(hit.getDirection());
        Key key = new Key(data.structure(), BlockPos.ZERO, data.rotation());
        Ghost ghost = PREVIEWS.get(key);
        if (ghost == null) {
            ghost = new Ghost();
            rebuild(minecraft, level, key, structure.get().placedIn(BlockPos.ZERO, key.rotation()), ghost);
            PREVIEWS.put(key, ghost);
        }

        drawGhost(event, origin, ghost, camera);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        LevelRenderer.renderLineBox(poseStack, lines, structure.get().worldBounds(origin, key.rotation()), 0.4f, 1f, 0.8f, 0.7f);
        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
    }

    private static List<Key> activeKeys(LocalPlayer player, ClientLevel level) {
        Set<Key> keys = new HashSet<>();
        for (InteractionHand hand : InteractionHand.values()) {
            SchematicData data = player.getItemInHand(hand).get(CraftorioDataComponents.SCHEMATIC.get());
            if (data == null || data.origin().isEmpty() || !data.origin().get().dimension().equals(level.dimension())) continue;
            keys.add(new Key(data.structure(), data.origin().get().pos(), data.rotation()));
        }
        return new ArrayList<>(keys);
    }

    private static void drawOriginMarker(RenderLevelStageEvent event, Minecraft minecraft, BlockPos origin, Vec3 camera) {
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        LevelRenderer.renderLineBox(poseStack, lines, new AABB(origin).inflate(0.02), 0.4f, 1f, 0.8f, 0.9f);
        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) return;

        List<Key> keys = activeKeys(player, level);
        GHOSTS.entrySet().removeIf(entry -> {
            if (keys.contains(entry.getKey())) return false;
            entry.getValue().close();
            return true;
        });

        Vec3 camera = event.getCamera().getPosition();
        long gameTime = level.getGameTime();

        SchematicData held = heldData(player);
        if (held != null && held.origin().isEmpty()) {
            renderPreview(event, minecraft, level, held, camera);
        }
        if (keys.isEmpty()) return;

        for (Key key : keys) {
            drawOriginMarker(event, minecraft, key.origin(), camera);
            Optional<SchematicStructure> structure = ClientSchematics.get(key.structure());
            if (structure.isEmpty()) continue;

            Ghost ghost = GHOSTS.computeIfAbsent(key, ignored -> new Ghost());
            if (ghost.lastRefresh == Long.MIN_VALUE || gameTime - ghost.lastRefresh >= REFRESH_TICKS || gameTime < ghost.lastRefresh) {
                ghost.lastRefresh = gameTime;
                refresh(minecraft, level, key, structure.get(), ghost);
            }

            drawGhost(event, key.origin(), ghost, camera);
            drawOutlines(event, minecraft, ghost, camera);
        }
    }

    private static void refresh(Minecraft minecraft, ClientLevel level, Key key, SchematicStructure structure, Ghost ghost) {
        List<SchematicStructure.Placed> placed = structure.placedIn(key.origin(), key.rotation());
        List<SchematicStructure.Placed> missing = new ArrayList<>();
        List<BlockPos> wrong = new ArrayList<>();
        long hash = 17L;
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
            if (SchematicStructure.matches(entry.state(), actual)) continue;

            missing.add(entry);
            hash = hash * 31L + pos.asLong();
            if (!actual.isAir() && !actual.canBeReplaced()) {
                wrong.add(pos);
            }
        }

        ghost.total = placed.size();
        ghost.placed = placed.size() - missing.size();
        ghost.wrong = wrong;
        ghost.bounds = placed.isEmpty() ? null : new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

        if (hash == ghost.missingHash && ghost.buffer != null) return;
        ghost.missingHash = hash;
        rebuild(minecraft, level, key, missing, ghost);
    }

    private static void rebuild(Minecraft minecraft, ClientLevel level, Key key, List<SchematicStructure.Placed> missing, Ghost ghost) {
        ghost.close();
        List<BlockPos> unrenderable = new ArrayList<>();
        if (missing.isEmpty()) {
            ghost.unrenderable = unrenderable;
            return;
        }

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(Math.max(4096, missing.size() * 1536))) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            GhostConsumer consumer = new GhostConsumer(builder);
            PoseStack poseStack = new PoseStack();

            for (SchematicStructure.Placed entry : missing) {
                BlockState state = entry.state();
                if (state.getRenderShape() != RenderShape.MODEL) {
                    unrenderable.add(entry.pos());
                    continue;
                }

                int tint = minecraft.getBlockColors().getColor(state, level, entry.pos(), 0);
                float red = tint == -1 ? 1f : ((tint >> 16) & 0xFF) / 255f;
                float green = tint == -1 ? 1f : ((tint >> 8) & 0xFF) / 255f;
                float blue = tint == -1 ? 1f : (tint & 0xFF) / 255f;

                poseStack.pushPose();
                poseStack.translate(entry.pos().getX() - key.origin().getX(), entry.pos().getY() - key.origin().getY(), entry.pos().getZ() - key.origin().getZ());
                dispatcher.getModelRenderer().renderModel(poseStack.last(), consumer, state, dispatcher.getBlockModel(state),
                        red, green, blue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
                poseStack.popPose();
            }

            MeshData mesh = builder.build();
            if (mesh != null) {
                VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                buffer.bind();
                buffer.upload(mesh);
                VertexBuffer.unbind();
                ghost.buffer = buffer;
            }
        }
        ghost.unrenderable = unrenderable;
    }

    private static void drawGhost(RenderLevelStageEvent event, BlockPos origin, Ghost ghost, Vec3 camera) {
        if (ghost.buffer == null) return;

        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix()).translate(
                (float) (origin.getX() - camera.x), (float) (origin.getY() - camera.y), (float) (origin.getZ() - camera.z));

        RenderType type = RenderType.translucent();
        type.setupRenderState();
        ghost.buffer.bind();
        ghost.buffer.drawWithShader(modelView, event.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
        type.clearRenderState();
    }

    private static void drawOutlines(RenderLevelStageEvent event, Minecraft minecraft, Ghost ghost, Vec3 camera) {
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (BlockPos pos : ghost.wrong) {
            DebugRenderer.renderFilledBox(poseStack, bufferSource, new AABB(pos).inflate(0.002), 1f, 0.1f, 0.1f, 0.35f);
        }
        bufferSource.endBatch(RenderType.debugFilledBox());

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        if (ghost.bounds != null) {
            LevelRenderer.renderLineBox(poseStack, lines, ghost.bounds, 0.4f, 0.8f, 1f, 0.6f);
        }
        for (BlockPos pos : ghost.wrong) {
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(pos).inflate(0.002), 1f, 0.2f, 0.2f, 1f);
        }
        for (BlockPos pos : ghost.unrenderable) {
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(pos).deflate(0.1), 0.6f, 0.9f, 1f, 0.8f);
        }
        bufferSource.endBatch(RenderType.lines());

        poseStack.popPose();
    }

    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Craftorio.prefix("schematic_progress"), (graphics, deltaTracker) -> renderHud(graphics));
    }

    private static void renderHud(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui) return;

        SchematicData data = null;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            data = stack.get(CraftorioDataComponents.SCHEMATIC.get());
            if (data != null) break;
        }
        if (data == null) return;

        Component line;
        if (data.origin().isEmpty()) {
            line = Component.translatable("misc.craftorio.schematic_help_place").withStyle(ChatFormatting.YELLOW);
        } else {
            Ghost ghost = GHOSTS.get(new Key(data.structure(), data.origin().get().pos(), data.rotation()));
            if (ghost == null) {
                line = Component.translatable("misc.craftorio.schematic_loading").withStyle(ChatFormatting.GRAY);
            } else {
                line = Component.translatable("misc.craftorio.schematic_progress", ghost.placed, ghost.total)
                        .withStyle(ghost.placed >= ghost.total ? ChatFormatting.GREEN : ChatFormatting.GOLD);
                if (!ghost.wrong.isEmpty()) {
                    line = line.copy().append(Component.translatable("misc.craftorio.schematic_wrong", ghost.wrong.size()).withStyle(ChatFormatting.RED));
                }
                if (ghost.total > 0 && ghost.placed >= ghost.total) {
                    line = line.copy().append(Component.translatable("misc.craftorio.schematic_ready", Component.keybind("key.craftorio.print_scan")).withStyle(ChatFormatting.GREEN));
                }
            }
        }

        int y = graphics.guiHeight() - 72;
        graphics.drawCenteredString(minecraft.font, line, graphics.guiWidth() / 2, y, 0xFFFFFF);
    }
}
