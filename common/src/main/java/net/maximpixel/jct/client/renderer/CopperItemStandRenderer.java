package net.maximpixel.jct.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.maximpixel.jct.block.entity.CopperItemStandBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class CopperItemStandRenderer implements BlockEntityRenderer<CopperItemStandBlockEntity> {

    protected final BlockEntityRendererProvider.Context context;

    public CopperItemStandRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(CopperItemStandBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1F + Mth.sin((blockEntity.getAge() + f) / 10F) * 0.1F, 0.5F);
        poseStack.mulPose(Axis.YP.rotation((blockEntity.getAge() + f) / 20F));
        BakedModel model = context.getItemRenderer().getModel(blockEntity.getItem(0), blockEntity.getLevel(), null, 0);
        context.getItemRenderer().render(blockEntity.getItem(0), ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }
}
