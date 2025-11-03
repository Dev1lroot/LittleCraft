package fr.dev1lroot.mcmods.littlecraft.model;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

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
        else if(entity.isVisuallySwimming() || entity.isVisuallyCrawling())
        {
            this.primary.xRot = this.overlay.xRot = pi;
            this.primary.z    = this.overlay.z    = -10F;
            //this.primary.xRot = this.overlay.xRot = (float) Math.toRadians(entity.getXRot());
        }
        else if (entity.hasPose(Pose.SLEEPING))
        {
            this.reset();

            if(!(entity instanceof Player)) return;

            Player player = ((Player) entity);
            Direction dir = player.getBedOrientation();

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

            this.primary.y    = this.overlay.y    = 0.0F;
            this.primary.xRot = this.overlay.xRot = pi - (pi/2);
        }
    }
    public void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float yaw, float partialTicks)
    {
        // Насколько игрок сейчас "в режиме плавания" (0..1)
        float swimAmount = player.getSwimAmount(partialTicks);

        // Угол наклона взгляда (pitch) с интерполяцией
        float viewPitch = player.getViewXRot(partialTicks);

        // --- Полет на элитрах ---
        if (player.isFallFlying())
        {
            float flightTicks = (float) player.getFallFlyingTicks() + partialTicks;
            float flightProgress = Mth.clamp(flightTicks * flightTicks / 100.0F, 0.0F, 1.0F);

            // Если не выполняется автоспиновая атака
            if (!player.isAutoSpinAttack())
            {
                // Наклон тела игрока вперед
                poseStack.mulPose(Axis.XP.rotationDegrees(-(flightProgress * (-90.0F - viewPitch))));
            }

            // Коррекция поворота тела по направлению фактического движения
            Vec3 lookVec = player.getViewVector(partialTicks);
            Vec3 motionVec = player.getDeltaMovementLerped(partialTicks);

            double motionLen = motionVec.horizontalDistanceSqr();
            double lookLen = lookVec.horizontalDistanceSqr();

            if (motionLen > 0.0 && lookLen > 0.0)
            {
                double dot = (motionVec.x * lookVec.x + motionVec.z * lookVec.z) / Math.sqrt(motionLen * lookLen);
                double cross = motionVec.x * lookVec.z - motionVec.z * lookVec.x;

                // Поворот вокруг оси Y (тело "поворачивается" по траектории)
                poseStack.mulPose(Axis.YP.rotation((float)(Math.signum(cross) * Math.acos(dot))));
            }
        }

        // --- Плавание ---
        else if (swimAmount > 0.0F)
        {
            // Базовый угол тела
            boolean inSwimmableFluid = player.isInWater() || player.isInFluidType((fluidType, height) -> player.canSwimInFluidType(fluidType));
            float baseAngle = inSwimmableFluid ? -90.0F - player.getXRot() : -90.0F;

            // Плавный переход от стоячего положения к плаванию
            float bodyPitch = Mth.lerp(swimAmount, 0.0F, baseAngle);
            poseStack.mulPose(Axis.XP.rotationDegrees(-bodyPitch));

            // Сдвиг модели вперед и вниз, чтобы "лежать" на воде
            if (player.isVisuallySwimming())
            {
                poseStack.translate(0.0F, -1.0F, 0.3F);
            }
        }
    }

}

