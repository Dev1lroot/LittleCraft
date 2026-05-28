/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class ThighHighsModel extends EntityModel<HumanoidRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(MODID, "thigh_highs_model"), "main");

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MODID, "textures/models/armor/thigh_highs.png");
    public static final Identifier TEXTURE_STRIPES =
            Identifier.fromNamespaceAndPath(MODID, "textures/models/armor/thigh_highs_stripes.png");

    public final ModelPart baseRight;
    public final ModelPart baseLeft;
    public final ModelPart stripeRight;
    public final ModelPart stripeLeft;

    public ThighHighsModel(ModelPart root)
    {
        super(root);
        this.baseRight  = root.getChild("base_right");
        this.baseLeft   = root.getChild("base_left");
        this.stripeRight = root.getChild("stripe_right");
        this.stripeLeft  = root.getChild("stripe_left");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Base layer — slightly inflated (0.5F) so it sits just outside the player leg.
        // Right leg pivot: (-1.9, 12, 0) matching vanilla PlayerModel right_leg.
        root.addOrReplaceChild("base_right",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.38F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        // Left leg pivot: (1.9, 12, 0)
        root.addOrReplaceChild("base_left",
                CubeListBuilder.create()
                        .texOffs(16, 0)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.38F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        // Stripe layer — slightly more inflated (0.6F) so it renders on top of the base.
        root.addOrReplaceChild("stripe_right",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.39F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild("stripe_left",
                CubeListBuilder.create()
                        .texOffs(16, 0)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.39F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(HumanoidRenderState state)
    {
        reset();

        float animPos   = state.walkAnimationPos;
        float animSpeed = state.walkAnimationSpeed;

        // Mirror vanilla HumanoidModel leg walk animation.
        this.baseRight.xRot  = Mth.cos(animPos * 0.6662F) * 1.4F * animSpeed;
        this.baseLeft.xRot   = Mth.cos(animPos * 0.6662F + (float) Math.PI) * 1.4F * animSpeed;
        this.stripeRight.xRot = this.baseRight.xRot;
        this.stripeLeft.xRot  = this.baseLeft.xRot;

        if (state.isCrouching)
        {
            // Shift the stockings forward/down to match the crouching body pose.
        //     this.baseRight.xRot  += 0.4F;
        //     this.baseLeft.xRot   += 0.4F;
        //     this.stripeRight.xRot += 0.4F;
        //     this.stripeLeft.xRot  += 0.4F;

            this.baseRight.z  = 4.0F;
            this.baseLeft.z   = 4.0F;
            this.stripeRight.z = 4.0F;
            this.stripeLeft.z  = 4.0F;
        }
    }

    private void reset()
    {
        this.baseRight.xRot  = this.baseLeft.xRot  = 0.0F;
        this.baseRight.yRot  = this.baseLeft.yRot  = 0.0F;
        this.baseRight.zRot  = this.baseLeft.zRot  = 0.0F;
        this.stripeRight.xRot = this.stripeLeft.xRot = 0.0F;
        this.stripeRight.yRot = this.stripeLeft.yRot = 0.0F;
        this.stripeRight.zRot = this.stripeLeft.zRot = 0.0F;

        this.baseRight.z  = this.baseLeft.z  = 0.0F;
        this.stripeRight.z = this.stripeLeft.z = 0.0F;
    }
}
