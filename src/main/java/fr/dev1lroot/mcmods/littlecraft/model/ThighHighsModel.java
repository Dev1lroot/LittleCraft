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
        // Pose is driven by ThighHighsLayer.syncLeg() — nothing to do here.
    }
}
