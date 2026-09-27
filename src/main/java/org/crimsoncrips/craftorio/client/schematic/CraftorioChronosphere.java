package org.crimsoncrips.craftorio.client.schematic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.crimsoncrips.craftorio.client.render.CraftorioShaders;
import org.crimsoncrips.craftorio.networking.schematic.ChronospherePacket;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class CraftorioChronosphere {

    private static final int LATITUDES = 90;
    private static final int LONGITUDES = 180;
    private static final int REMOVAL_TICK = ChronospherePacket.APPEAR_TICKS + ChronospherePacket.HOLD_TICKS;
    private static final int TOTAL_TICKS = REMOVAL_TICK + ChronospherePacket.DISSIPATE_TICKS;

    private record Chronosphere(ResourceKey<Level> dimension, Vec3 center, float radius, long start) {}

    private static final List<Chronosphere> CHRONOSPHERES = new ArrayList<>();
    private static VertexBuffer sphere;

    private CraftorioChronosphere() {}

    public static void add(double x, double y, double z, float radius) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        CHRONOSPHERES.add(new Chronosphere(level.dimension(), new Vec3(x, y, z), radius, level.getGameTime()));
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CHRONOSPHERES.clear();
    }

    private static void ensureSphere() {
        if (sphere != null) return;

        try (ByteBufferBuilder bytes = new ByteBufferBuilder(LATITUDES * LONGITUDES * 4 * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            for (int lat = 0; lat < LATITUDES; lat++) {
                float phi0 = (float) Math.PI * (lat / (float) LATITUDES - 0.5f);
                float phi1 = (float) Math.PI * ((lat + 1) / (float) LATITUDES - 0.5f);
                for (int lon = 0; lon < LONGITUDES; lon++) {
                    float theta0 = (float) (2 * Math.PI * lon / LONGITUDES);
                    float theta1 = (float) (2 * Math.PI * (lon + 1) / LONGITUDES);
                    vertex(builder, phi0, theta0);
                    vertex(builder, phi0, theta1);
                    vertex(builder, phi1, theta1);
                    vertex(builder, phi1, theta0);
                }
            }

            MeshData mesh = builder.build();
            if (mesh == null) return;
            sphere = new VertexBuffer(VertexBuffer.Usage.STATIC);
            sphere.bind();
            sphere.upload(mesh);
            VertexBuffer.unbind();
        }
    }

    private static void vertex(BufferBuilder builder, float phi, float theta) {
        float cos = Mth.cos(phi);
        builder.addVertex(cos * Mth.cos(theta), Mth.sin(phi), cos * Mth.sin(theta));
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        if (CHRONOSPHERES.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        ShaderInstance shader = CraftorioShaders.chronosphere();
        if (level == null || shader == null) return;

        float now = level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        CHRONOSPHERES.removeIf(chronosphere -> now - chronosphere.start() >= TOTAL_TICKS);
        ensureSphere();
        if (sphere == null) return;

        Vec3 camera = event.getCamera().getPosition();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        sphere.bind();
        for (Chronosphere chronosphere : CHRONOSPHERES) {
            if (!chronosphere.dimension().equals(level.dimension())) continue;

            float elapsed = now - chronosphere.start();
            float appear = Mth.clamp(elapsed / ChronospherePacket.APPEAR_TICKS, 0f, 1f);
            appear = 1f - (1f - appear) * (1f - appear);
            float dissipate = Mth.clamp((elapsed - REMOVAL_TICK) / ChronospherePacket.DISSIPATE_TICKS, 0f, 1f);

            Matrix4f modelView = new Matrix4f(event.getModelViewMatrix())
                    .translate((float) (chronosphere.center().x - camera.x), (float) (chronosphere.center().y - camera.y), (float) (chronosphere.center().z - camera.z))
                    .scale(chronosphere.radius());
            shader.safeGetUniform("ChronosphereParams").set(elapsed / 20f, appear, dissipate, 1f);
            sphere.drawWithShader(modelView, event.getProjectionMatrix(), shader);
        }
        VertexBuffer.unbind();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
