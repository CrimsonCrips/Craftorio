package org.crimsoncrips.craftorio.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.crimsoncrips.craftorio.block.entity.AreaScannerBlockEntity;

@OnlyIn(Dist.CLIENT)
public class AreaScannerBlockEntityRenderer implements BlockEntityRenderer<AreaScannerBlockEntity> {

    public AreaScannerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AreaScannerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        double minX = blockEntity.getOffsetX();
        double minY = blockEntity.getOffsetY();
        double minZ = blockEntity.getOffsetZ();
        double maxX = minX + blockEntity.getSizeX();
        double maxY = minY + blockEntity.getSizeY();
        double maxZ = minZ + blockEntity.getSizeZ();

        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, vertexconsumer, minX, minY, minZ, maxX, maxY, maxZ, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean shouldRenderOffScreen(AreaScannerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(AreaScannerBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
