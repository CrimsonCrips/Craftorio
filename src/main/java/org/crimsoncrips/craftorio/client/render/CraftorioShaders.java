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

    private static ShaderInstance skillTreeStarfield;
    private static ShaderInstance shatterEye;
    private static ShaderInstance chronosphere;
    private static ShaderInstance loanSharkAura;
    private static ShaderInstance sacrificeGlow;
    private static ShaderInstance sacrificeElectric;
    private static ShaderInstance sacrificePulse;

    private CraftorioShaders() {}

    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("skill_tree_starfield"), DefaultVertexFormat.POSITION),
                    shader -> skillTreeStarfield = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("shatter_eye"), DefaultVertexFormat.POSITION),
                    shader -> shatterEye = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("chronosphere"), DefaultVertexFormat.POSITION),
                    shader -> chronosphere = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("loan_shark_aura"), DefaultVertexFormat.POSITION_TEX),
                    shader -> loanSharkAura = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("sacrifice_glow"), DefaultVertexFormat.POSITION_TEX),
                    shader -> sacrificeGlow = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("sacrifice_electric"), DefaultVertexFormat.POSITION_TEX),
                    shader -> sacrificeElectric = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Craftorio.prefix("sacrifice_pulse"), DefaultVertexFormat.POSITION_TEX),
                    shader -> sacrificePulse = shader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ShaderInstance skillTreeStarfield() {
        return skillTreeStarfield;
    }

    public static ShaderInstance shatterEye() {
        return shatterEye;
    }

    public static ShaderInstance chronosphere() {
        return chronosphere;
    }

    public static ShaderInstance loanSharkAura() {
        return loanSharkAura;
    }

    public static ShaderInstance sacrificeGlow() {
        return sacrificeGlow;
    }

    public static ShaderInstance sacrificeElectric() {
        return sacrificeElectric;
    }

    public static ShaderInstance sacrificePulse() {
        return sacrificePulse;
    }
}
