package org.crimsoncrips.craftorio.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.crimsoncrips.craftorio.Craftorio;

import java.io.IOException;
import java.io.UncheckedIOException;

@OnlyIn(Dist.CLIENT)
public final class CraftorioShaders {

    private static ShaderInstance skillTreeStarNest;

    private CraftorioShaders() {}

    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("skill_tree_starnest"), DefaultVertexFormat.POSITION),
                    shader -> skillTreeStarNest = shader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ShaderInstance skillTreeStarNest() {
        return skillTreeStarNest;
    }
}
