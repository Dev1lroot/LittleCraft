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

// Behold! The mighty DiaperModel.
// It bravely wraps around your Minecraft entity's butt
// and provides both fluff and a magical wetness indicator.
public class DiaperModel<T extends Entity> extends EntityModel<T> {

    // The sacred diaper layer location -
    // basically the blueprint where we summon the diaper parts.
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MODID, "diaper_model"), "main");

    // The strong and sturdy base diaper part. (The actual puff.)
    public final ModelPart primary;

    // The mystical wetness indicator - becomes visible when things get... soggy.
    public final ModelPart overlay;

    // Diaper usage meter.
    // 0 = fresh and fluffy, 255 = "uh oh, it's definitely time for a change".
    public int damage = 0;

    // Constructor - puts the diaper parts together. No duct tape required.
    public DiaperModel(ModelPart root) {
        this.primary = root.getChild("primary");
        this.overlay = root.getChild("overlay");
    }

    // Builds the diaper's 3D layers.
    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition primary = partdefinition.addOrReplaceChild("primary",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -14.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition overlay = partdefinition.addOrReplaceChild("overlay",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -14.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    // This is where the diaper gets drawn on-screen.
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb)
    {
        // Primary layer = the fluffy base, actual design layer
        ResourceLocation primaryTexture = ResourceLocation.parse(MODID + ":textures/texture2.png"); //test
        VertexConsumer primaryConsumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.entityCutout(primaryTexture));

        primary.render(poseStack, primaryConsumer, packedLight, packedOverlay, rgb);

        // Overlay = the wetness indicator that fades in with usage.
        ResourceLocation overlayTexture = ResourceLocation.parse(MODID + ":textures/texture2-overlay.png"); //test
        VertexConsumer overlayConsumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.entityTranslucent(overlayTexture));

        // Alpha channel = wetness level based on how "used" the diaper is.
        int alpha = damage; // 0..255
        int rgba = (alpha << 24) | (rgb & 0xFFFFFF);

        overlay.render(poseStack, overlayConsumer, packedLight, packedOverlay, rgba);
    }

    // Fixes vanilla offsetting (upsetting) while crouch or other animations
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if(entity.isCrouching())
        {
            this.primary.y = 16.0F;
            this.primary.z = -8.0F;
            this.overlay.y = 16.0F;
            this.overlay.z = -8.0F;
        }
        else
        {
            this.primary.y = 24.0F;
            this.primary.z = 0.0F;
            this.overlay.y = 24.0F;
            this.overlay.z = 0.0F;
        }
    }
}

