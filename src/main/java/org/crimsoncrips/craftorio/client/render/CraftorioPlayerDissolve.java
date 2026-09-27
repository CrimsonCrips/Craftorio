package org.crimsoncrips.craftorio.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class CraftorioPlayerDissolve {

    public static final int FULL_WHITE_OVERLAY_V = 15;
    private static final long WHITEN_MS = 1300L;
    private static final long UNSHATTERED_EXPIRE_MS = 15000L;
    private static final long SHATTERED_EXPIRE_MS = 6000L;
    private static final int SHARD_COUNT = 160;
    private static final int FULL_LIGHT = 15 << 4;
    private static final long CONVERGE_MS = 700L;
    private static final long UNWHITEN_MS = 1300L;
    private static final long ASSEMBLE_WAIT_MS = 4000L;
    private static final float DESCENT = 1.2f;
    private static final double CONVERGE_MIN_RADIUS = 1.5;
    private static final double CONVERGE_MAX_RADIUS = 3.0;

    private static final Map<Integer, Long> starts = new HashMap<>();
    private static final Map<Integer, Long> shattered = new HashMap<>();
    private static final Map<Integer, Long> pendingAssembles = new HashMap<>();
    private static final Map<Integer, Long> assembles = new HashMap<>();
    private static final RandomSource random = RandomSource.create();
    private static boolean rendering = false;

    private CraftorioPlayerDissolve() {}

    public static void start(int entityId) {
        starts.put(entityId, Util.getMillis());
        shattered.remove(entityId);
    }

    public static void shatter(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || shattered.containsKey(entityId)) return;

        starts.putIfAbsent(entityId, Util.getMillis());
        shattered.put(entityId, Util.getMillis());
        Entity entity = level.getEntity(entityId);
        if (entity != null) {
            shatter(level, entity);
        }
    }

    public static void assemble(int entityId) {
        starts.remove(entityId);
        shattered.remove(entityId);
        assembles.remove(entityId);
        pendingAssembles.put(entityId, Util.getMillis());
    }

    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (rendering) return;

        int id = event.getEntity().getId();
        if (pendingAssembles.containsKey(id)) {
            event.setCanceled(true);
            return;
        }

        Long assembleStart = assembles.get(id);
        if (assembleStart != null) {
            event.setCanceled(true);
            long elapsed = Util.getMillis() - assembleStart - CONVERGE_MS;
            if (elapsed < 0) return;

            float settle = Mth.clamp(elapsed / (float) UNWHITEN_MS, 0f, 1f);
            float remaining = 1f - settle;
            renderWhitened(event, remaining, DESCENT * remaining * remaining);
            return;
        }

        Long start = starts.get(id);
        if (start == null) return;

        event.setCanceled(true);
        if (shattered.containsKey(id)) return;

        long elapsed = Util.getMillis() - start;
        renderWhitened(event, Mth.clamp(elapsed / (float) WHITEN_MS, 0f, 1f), 0f);
    }

    private static void renderWhitened(RenderPlayerEvent.Pre event, float progress, float lift) {
        MultiBufferSource source = event.getMultiBufferSource();
        MultiBufferSource whitened = renderType -> new WhiteningConsumer(source.getBuffer(renderType), progress);

        rendering = true;
        event.getPoseStack().pushPose();
        try {
            event.getPoseStack().translate(0f, lift, 0f);
            AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
            event.getRenderer().render(player, player.getYRot(), event.getPartialTick(), event.getPoseStack(), whitened, event.getPackedLight());
        } finally {
            event.getPoseStack().popPose();
            rendering = false;
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (starts.isEmpty() && pendingAssembles.isEmpty() && assembles.isEmpty()) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            clear();
            return;
        }

        long now = Util.getMillis();
        tickAssembles(level, now);
        Iterator<Map.Entry<Integer, Long>> iterator = starts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            Long shatteredAt = shattered.get(entry.getKey());
            boolean expired = shatteredAt != null
                    ? now - shatteredAt >= SHATTERED_EXPIRE_MS
                    : now - entry.getValue() >= UNSHATTERED_EXPIRE_MS;
            if (expired) {
                shattered.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    private static void tickAssembles(ClientLevel level, long now) {
        Iterator<Map.Entry<Integer, Long>> pending = pendingAssembles.entrySet().iterator();
        while (pending.hasNext()) {
            Map.Entry<Integer, Long> entry = pending.next();
            Entity entity = level.getEntity(entry.getKey());
            if (entity != null) {
                converge(level, entity);
                assembles.put(entry.getKey(), now);
                pending.remove();
            } else if (now - entry.getValue() >= ASSEMBLE_WAIT_MS) {
                pending.remove();
            }
        }

        assembles.values().removeIf(start -> now - start >= CONVERGE_MS + UNWHITEN_MS);
    }

    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;

        int id = event.getEntity().getId();
        if (shattered.containsKey(id)) {
            starts.remove(id);
            shattered.remove(id);
        }
    }

    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        clear();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        starts.clear();
        shattered.clear();
        pendingAssembles.clear();
        assembles.clear();
    }

    private static void converge(ClientLevel level, Entity entity) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.SNOW_BLOCK.defaultBlockState());
        AABB box = entity.getBoundingBox();
        int lifetime = (int) (CONVERGE_MS / 50L);

        for (int i = 0; i < SHARD_COUNT; i++) {
            double targetX = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
            double targetY = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
            double targetZ = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
            double theta = random.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
            double radius = Mth.lerp(random.nextDouble(), CONVERGE_MIN_RADIUS, CONVERGE_MAX_RADIUS);
            double startX = targetX + Math.sin(phi) * Math.cos(theta) * radius;
            double startY = targetY + Math.cos(phi) * radius;
            double startZ = targetZ + Math.sin(phi) * Math.sin(theta) * radius;
            Minecraft.getInstance().particleEngine.add(new ConvergingShardParticle(level, startX, startY, startZ,
                    targetX, targetY, targetZ, lifetime - random.nextInt(4), sprite));
        }
    }

    private static void shatter(ClientLevel level, Entity entity) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.SNOW_BLOCK.defaultBlockState());
        AABB box = entity.getBoundingBox();
        double centerX = (box.minX + box.maxX) / 2.0;
        double centerZ = (box.minZ + box.maxZ) / 2.0;

        for (int i = 0; i < SHARD_COUNT; i++) {
            double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
            double y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
            double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
            double speed = 0.04 + random.nextDouble() * 0.08;
            double dx = (x - centerX) * 2.0 + (random.nextDouble() - 0.5) * 0.4;
            double dz = (z - centerZ) * 2.0 + (random.nextDouble() - 0.5) * 0.4;
            double length = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
            Minecraft.getInstance().particleEngine.add(new WhiteShardParticle(level, x, y, z,
                    dx / length * speed, 0.01 + random.nextDouble() * 0.06, dz / length * speed, sprite));
        }
    }

    private record WhiteningConsumer(VertexConsumer delegate, float progress) implements VertexConsumer {

        private int overlay() {
            return OverlayTexture.pack(OverlayTexture.u(this.progress), FULL_WHITE_OVERLAY_V);
        }

        private int light(int packedLight) {
            int block = Math.round(Mth.lerp(this.progress, packedLight & 0xFFFF, FULL_LIGHT));
            int sky = Math.round(Mth.lerp(this.progress, packedLight >> 16 & 0xFFFF, FULL_LIGHT));
            return block | sky << 16;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            this.delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.delegate.setOverlay(overlay());
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.delegate.setLight(light(u | v << 16));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            this.delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
            this.delegate.addVertex(x, y, z, color, u, v, overlay(), light(packedLight), normalX, normalY, normalZ);
        }
    }

    private static final class ConvergingShardParticle extends WhiteShardParticle {

        private final double startX;
        private final double startY;
        private final double startZ;
        private final double targetX;
        private final double targetY;
        private final double targetZ;

        private ConvergingShardParticle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ,
                                        int lifetime, TextureAtlasSprite sprite) {
            super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
            this.startX = x;
            this.startY = y;
            this.startZ = z;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.lifetime = Math.max(1, lifetime);
            this.quadSize = 0f;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (this.age++ >= this.lifetime) {
                this.remove();
                return;
            }
            float t = this.age / (float) this.lifetime;
            double eased = t * t;
            this.setPos(Mth.lerp(eased, this.startX, this.targetX), Mth.lerp(eased, this.startY, this.targetY), Mth.lerp(eased, this.startZ, this.targetZ));
            this.quadSize = this.startSize * t;
            this.alpha = 1f;
        }
    }

    private static class WhiteShardParticle extends TextureSheetParticle {

        protected final float startSize;

        protected WhiteShardParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
            super(level, x, y, z);
            setSprite(sprite);
            this.xd = xd;
            this.yd = yd;
            this.zd = zd;
            this.hasPhysics = false;
            this.gravity = -0.3f;
            this.friction = 0.93f;
            this.lifetime = 20 + this.random.nextInt(20);
            this.startSize = 0.04f + this.random.nextFloat() * 0.06f;
            this.quadSize = this.startSize;
        }

        @Override
        public void tick() {
            super.tick();
            float life = Mth.clamp(this.age / (float) this.lifetime, 0f, 1f);
            this.quadSize = this.startSize * (1f - life);
            this.alpha = 1f - life * life;
        }

        @Override
        protected float getU0() {
            return this.sprite.getU(0.25f);
        }

        @Override
        protected float getU1() {
            return this.sprite.getU(0.5f);
        }

        @Override
        protected float getV0() {
            return this.sprite.getV(0.25f);
        }

        @Override
        protected float getV1() {
            return this.sprite.getV(0.5f);
        }

        @Override
        public int getLightColor(float partialTick) {
            return LightTexture.FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.TERRAIN_SHEET;
        }
    }
}
