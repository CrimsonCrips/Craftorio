package org.crimsoncrips.craftorio.client.schematic;

import org.crimsoncrips.craftorio.server.schematic.CraftorioSchematics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
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
    private static final long MAX_INITIAL_BUFFER = 16L * 1024 * 1024;

    private record Key(ResourceLocation structure, BlockPos origin, Rotation rotation) {}

    private static final class Ghost {
        VertexBuffer buffer;
        long missingHash = Long.MIN_VALUE;
        SchematicVerifier.Result result;
        List<SchematicStructure.Placed> unrenderable = List.of();
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

    private static final double TARGET_RANGE = 24.0;
    private static final SchematicVerifier.Issue BLOCKED = new SchematicVerifier.Issue(SchematicVerifier.Kind.MISSING, BlockPos.ZERO, null, null);

    private static final long BLITZ_FADE_IN_MS = 1000L;
    private static boolean blitzHidden;
    private static long blitzFadeInStart = -1L;

    private static Component lastTargetMessage;
    private static long lastTargetShown;

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
            if (data != null && CraftorioSchematics.isLinked(player, data)) return data;
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

        drawGhost(event, origin, ghost, camera, 1f);

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
            if (!CraftorioSchematics.isLinked(player, data)) continue;
            keys.add(new Key(data.structure(), data.origin().get().pos(), data.rotation()));
        }
        return new ArrayList<>(keys);
    }

    private static void drawOriginMarker(RenderLevelStageEvent event, Minecraft minecraft, BlockPos origin, Vec3 camera, float visibility) {
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        LevelRenderer.renderLineBox(poseStack, lines, new AABB(origin).inflate(0.02), 0.4f, 1f, 0.8f, 0.9f * visibility);
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

        float visibility = ghostVisibility();
        if (visibility <= 0f) return;

        for (Key key : keys) {
            drawOriginMarker(event, minecraft, key.origin(), camera, visibility);
            Optional<SchematicStructure> structure = ClientSchematics.get(key.structure());
            if (structure.isEmpty()) continue;

            Ghost ghost = GHOSTS.computeIfAbsent(key, ignored -> new Ghost());
            if (ghost.lastRefresh == Long.MIN_VALUE || gameTime - ghost.lastRefresh >= REFRESH_TICKS || gameTime < ghost.lastRefresh) {
                ghost.lastRefresh = gameTime;
                refresh(minecraft, level, key, structure.get(), ghost);
            }

            drawGhost(event, key.origin(), ghost, camera, visibility);
            drawOutlines(event, minecraft, ghost, camera, visibility);
        }
    }

    private static float ghostVisibility() {
        if (BuildBlitzEffect.isLocalBlitzActive()) {
            blitzHidden = true;
            blitzFadeInStart = -1L;
            return 0f;
        }
        if (blitzHidden) {
            blitzHidden = false;
            blitzFadeInStart = Util.getMillis();
            GHOSTS.values().forEach(ghost -> ghost.lastRefresh = Long.MIN_VALUE);
        }
        if (blitzFadeInStart < 0L) return 1f;

        float progress = Mth.clamp((Util.getMillis() - blitzFadeInStart) / (float) BLITZ_FADE_IN_MS, 0f, 1f);
        if (progress >= 1f) {
            blitzFadeInStart = -1L;
        }
        return progress;
    }

    private static void refresh(Minecraft minecraft, ClientLevel level, Key key, SchematicStructure structure, Ghost ghost) {
        SchematicVerifier.Result result = SchematicVerifier.analyze(level, structure, key.origin(), key.rotation());
        ghost.result = result;

        if (result.ghostHash() == ghost.missingHash && (ghost.buffer != null || result.ghost().isEmpty())) return;
        ghost.missingHash = result.ghostHash();
        rebuild(minecraft, level, key, result.ghost(), ghost);
    }

    public static Optional<SchematicVerifier.Result> result(SchematicData data) {
        if (data.origin().isEmpty()) return Optional.empty();
        Ghost ghost = GHOSTS.get(new Key(data.structure(), data.origin().get().pos(), data.rotation()));
        return ghost == null ? Optional.empty() : Optional.ofNullable(ghost.result);
    }

    public static Optional<SchematicVerifier.Issue> targetedIssue(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || blitzHidden || BuildBlitzEffect.isLocalBlitzActive()) return Optional.empty();

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1f).scale(TARGET_RANGE));
        for (Key key : activeKeys(player, level)) {
            Ghost ghost = GHOSTS.get(key);
            if (ghost == null || ghost.result == null || ghost.result.byPos().isEmpty()) continue;
            Map<BlockPos, SchematicVerifier.Issue> issues = ghost.result.byPos();
            SchematicVerifier.Issue found = BlockGetter.traverseBlocks(eye, end, issues, (map, pos) -> {
                SchematicVerifier.Issue issue = map.get(pos);
                if (issue != null) return issue;
                BlockState state = level.getBlockState(pos);
                return state.isAir() || state.getShape(level, pos).isEmpty() ? null : BLOCKED;
            }, map -> null);
            if (found != null && found != BLOCKED) return Optional.of(found);
        }
        return Optional.empty();
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            lastTargetMessage = null;
            return;
        }

        Optional<SchematicVerifier.Issue> issue = targetedIssue(minecraft);
        if (issue.isEmpty()) {
            lastTargetMessage = null;
            return;
        }

        Component message = SchematicVerifier.describe(issue.get()).copy().withColor(issue.get().kind().color());
        long gameTime = minecraft.level.getGameTime();
        if (!message.equals(lastTargetMessage) || gameTime - lastTargetShown >= 20) {
            minecraft.gui.setOverlayMessage(message, false);
            lastTargetMessage = message;
            lastTargetShown = gameTime;
        }
    }

    private static void rebuild(Minecraft minecraft, ClientLevel level, Key key, List<SchematicStructure.Placed> missing, Ghost ghost) {
        ghost.close();
        if (missing.isEmpty()) {
            ghost.unrenderable = List.of();
            return;
        }

        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        List<SchematicStructure.Placed> unrenderableEntries = new ArrayList<>();
        try (ByteBufferBuilder bytes = new ByteBufferBuilder((int) Math.min(MAX_INITIAL_BUFFER, Math.max(4096L, missing.size() * 1536L)))) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            GhostConsumer consumer = new GhostConsumer(builder);
            PoseStack poseStack = new PoseStack();

            for (SchematicStructure.Placed entry : missing) {
                BlockState state = entry.state();
                if (state.getRenderShape() != RenderShape.MODEL) {
                    unrenderableEntries.add(entry);
                    if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) continue;
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
        ghost.unrenderable = unrenderableEntries;
    }

    private static void drawGhost(RenderLevelStageEvent event, BlockPos origin, Ghost ghost, Vec3 camera, float visibility) {
        if (ghost.buffer == null) return;

        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix()).translate(
                (float) (origin.getX() - camera.x), (float) (origin.getY() - camera.y), (float) (origin.getZ() - camera.z));

        RenderType type = RenderType.translucent();
        type.setupRenderState();
        RenderSystem.setShaderColor(1f, 1f, 1f, visibility);
        ghost.buffer.bind();
        ghost.buffer.drawWithShader(modelView, event.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        type.clearRenderState();
    }

    private static int unrenderableColor(BlockState state) {
        if (state.getFluidState().is(FluidTags.LAVA)) return 0xFF7A1A;
        if (!state.getFluidState().isEmpty()) {
            int tint = IClientFluidTypeExtensions.of(state.getFluidState()).getTintColor();
            return tint == 0xFFFFFFFF || tint == -1 ? 0x3F76E4 : tint & 0xFFFFFF;
        }
        return 0x55AAFF;
    }

    private static void drawOutlines(RenderLevelStageEvent event, Minecraft minecraft, Ghost ghost, Vec3 camera, float visibility) {
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        SchematicVerifier.Result result = ghost.result;
        if (result != null) {
            for (SchematicVerifier.Issue issue : result.issues()) {
                if (issue.kind() == SchematicVerifier.Kind.MISSING) continue;
                int color = issue.kind().color();
                DebugRenderer.renderFilledBox(poseStack, bufferSource, new AABB(issue.pos()).inflate(0.002),
                        ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 0.35f * visibility);
            }
        }
        for (SchematicStructure.Placed entry : ghost.unrenderable) {
            int color = unrenderableColor(entry.state());
            DebugRenderer.renderFilledBox(poseStack, bufferSource, new AABB(entry.pos()).deflate(0.05),
                    ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 0.3f * visibility);
        }
        bufferSource.endBatch(RenderType.debugFilledBox());

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());
        if (result != null && result.bounds() != null) {
            LevelRenderer.renderLineBox(poseStack, lines, result.bounds(), 0.4f, 0.8f, 1f, 0.6f * visibility);
        }
        if (result != null) {
            for (SchematicVerifier.Issue issue : result.issues()) {
                if (issue.kind() == SchematicVerifier.Kind.MISSING) continue;
                int color = issue.kind().color();
                LevelRenderer.renderLineBox(poseStack, lines, new AABB(issue.pos()).inflate(0.002),
                        ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, visibility);
            }
        }
        for (SchematicStructure.Placed entry : ghost.unrenderable) {
            int color = unrenderableColor(entry.state());
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(entry.pos()).deflate(0.05),
                    ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 0.9f * visibility);
        }
        Optional<SchematicVerifier.Issue> targeted = targetedIssue(minecraft);
        if (targeted.isPresent()) {
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(targeted.get().pos()).inflate(0.02), 1f, 1f, 1f, visibility);
        }
        bufferSource.endBatch(RenderType.lines());

        poseStack.popPose();
    }
}
