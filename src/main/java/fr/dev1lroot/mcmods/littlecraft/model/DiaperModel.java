package fr.dev1lroot.mcmods.littlecraft.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.WEST;

// Behold! The mighty DiaperModel.
// It bravely wraps around your Minecraft entity's butt
// and provides both fluff and a magical wetness indicator.
public class DiaperModel<T extends Entity> extends EntityModel<T>
{
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
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition overlay = partdefinition.addOrReplaceChild("overlay",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    // This is where the diaper gets drawn on-screen.
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb)
    {
        // Primary layer = the fluffy base, actual design layer
        ResourceLocation primaryTexture = ResourceLocation.parse(MODID + ":textures/diaper/default.png"); //test
        VertexConsumer primaryConsumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.entityCutout(primaryTexture));

        primary.render(poseStack, primaryConsumer, packedLight, packedOverlay, rgb);

        // Overlay = the wetness indicator that fades in with usage.
        ResourceLocation overlayTexture = ResourceLocation.parse(MODID + ":textures/diaper/wetness.png"); //test
        VertexConsumer overlayConsumer = Minecraft.getInstance().renderBuffers().bufferSource()
                .getBuffer(RenderType.entityTranslucent(overlayTexture));

        // Alpha channel = wetness level based on how "used" the diaper is.
        int alpha = damage; // 0..255
        int rgba = (alpha << 24) | (rgb & 0xFFFFFF);

        overlay.render(poseStack, overlayConsumer, packedLight, packedOverlay, rgba);

        this.reset();
    }

    private void reset()
    {
        float pi = (float) Math.PI;

        this.primary.y = this.overlay.y = 16.0F;
        this.primary.z = this.overlay.z = 0.0F;
        this.primary.x = this.overlay.x = 0.0F;

        this.primary.xRot = this.overlay.xRot = pi;
        this.primary.yRot = this.overlay.yRot = 0;
    }

    // Fixes vanilla offsetting (upsetting) while crouch or other animations
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        // Resets everything
        this.reset();

        // PI
        float pi = (float) Math.PI;

        if(entity instanceof Player && entity.isCrouching())
        {
            this.primary.y    = this.overlay.y    = 15.0F;
            this.primary.z    = this.overlay.z    = -2.5F;
            this.primary.xRot = this.overlay.xRot = pi + (pi/6);
        }
        else if (entity instanceof Player && ((Player) entity).isSleeping())
        {
            this.reset();

            Player player = ((Player) entity);

            // This is how's rendering hell is looks like:
            BlockPos bedPos = player.getSleepingPos().orElse(null);
            if (bedPos != null)
            {
                BlockState bedState = player.level().getBlockState(bedPos);
                if (bedState.getBlock() instanceof BedBlock)
                {
                    Direction dir = bedState.getValue(BedBlock.FACING);

                    if(dir == NORTH)
                    {
                        this.primary.yRot = this.overlay.yRot = -0.0F;
                        this.primary.z    = this.overlay.z    = 10.0F;
                    }
                    if(dir == SOUTH)
                    {
                        this.primary.yRot = this.overlay.yRot = -(float)Math.PI;
                        this.primary.z    = this.overlay.z    = -10.0F;
                    }
                    if(dir == WEST)
                    {
                        this.primary.yRot = this.overlay.yRot = -(float)(-Math.PI / 2.0F);
                        this.primary.x    = this.overlay.x    = 10.0F;
                    }
                    if(dir == EAST)
                    {
                        this.primary.yRot = this.overlay.yRot = -(float)(Math.PI / 2.0F);
                        this.primary.x    = this.overlay.x    = -10.0F;
                    }
                }
            }

            this.primary.y    = this.overlay.y    = 0.0F;
            this.primary.xRot = this.overlay.xRot = pi - (pi/2);
        }
    }
}

