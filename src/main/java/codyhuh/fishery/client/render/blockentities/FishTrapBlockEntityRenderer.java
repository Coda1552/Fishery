package codyhuh.fishery.client.render.blockentities;

import codyhuh.fishery.common.blockentities.FishTrapBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FishTrapBlockEntityRenderer implements BlockEntityRenderer<FishTrapBlockEntity> {

    public FishTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FishTrapBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity.getItems().isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);

        //RandomSource rand = RandomSource.create();
        //float f = rand.nextFloat();
        //poseStack.mulPose(Axis.ZP.rotation(f));

        for (int i = 0; i < blockEntity.countItems(blockEntity.getItems()); i++) {
            ItemStack stack = blockEntity.getItem(i);

            //poseStack.mulPose(Axis.ZP.rotation(i % 2 == 0 ? 0.65F : -0.65F));

            poseStack.translate(0.0D, 1.5D, 0.0D);

            BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, null, 0);

            mc.getItemRenderer().render(stack, ItemDisplayContext.GROUND, true, poseStack, buffer, combinedLight, combinedOverlay, model);
        }


        poseStack.popPose();
    }
}