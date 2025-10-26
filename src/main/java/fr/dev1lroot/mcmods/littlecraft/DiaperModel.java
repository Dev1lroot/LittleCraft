package fr.dev1lroot.mcmods.littlecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class DiaperModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MODID, "diaper_model"), "main");
    public final ModelPart primary;
    public final ModelPart overlay;

    public DiaperModel(ModelPart root) {
        this.primary = root.getChild("primary");
        this.overlay = root.getChild("overlay");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition primary = partdefinition.addOrReplaceChild("primary",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -14.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition overlay = partdefinition.addOrReplaceChild("overlay",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -14.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {

        ResourceLocation primaryTexture = ResourceLocation.parse(MODID + ":textures/texture2.png");
        VertexConsumer primaryConsumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.entityCutout(primaryTexture));

        primary.render(poseStack, primaryConsumer, packedLight, packedOverlay, rgb);

        ResourceLocation overlayTexture = ResourceLocation.parse(MODID + ":textures/texture2-overlay.png");
        VertexConsumer overlayConsumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.entityCutout(overlayTexture));

        overlay.render(poseStack, overlayConsumer, packedLight, packedOverlay, rgb);
    }


    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
