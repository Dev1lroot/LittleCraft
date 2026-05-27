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

public class DiaperModel extends EntityModel<HumanoidRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MODID, "diaper_model"), "main");

    public static final Identifier TEXTURE_PRIMARY = Identifier.fromNamespaceAndPath(MODID, "textures/diaper/default/default.png");
    public static final Identifier TEXTURE_WETNESS = Identifier.fromNamespaceAndPath(MODID, "textures/diaper/default/wetness.png");

    public final ModelPart primary;
    public final ModelPart overlay;

    public int damage = 0;

    public DiaperModel(ModelPart root) {
        super(root);
        this.primary = root.getChild("primary");
        this.overlay = root.getChild("overlay");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("primary",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("overlay",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(HumanoidRenderState state)
    {
        reset();

        if (state.isCrouching)
        {
            // body.xRot = +0.5F when crouching (lean forward); diaper matches.
            this.primary.y = this.overlay.y = 9.0F; // Moving it down
            this.primary.z = this.overlay.z = 3.0F; // Moving it back
            this.primary.xRot = this.overlay.xRot = 0.5F; // Rotating to the pose of the body element
        }
        // Swimming and sleeping rotations are handled by the entity renderer's setupRotations;
        // the diaper inherits those poseStack transforms automatically as a RenderLayer.
    }

    private void reset()
    {
        // y=8 places the box at model y=8–16 (the hip joint of a standard player model).
        this.primary.y = this.overlay.y = 8.0F;
        this.primary.z = this.overlay.z = 0.0F;
        this.primary.x = this.overlay.x = 0.0F;
        this.primary.xRot = this.overlay.xRot = 0.0F;
        this.primary.yRot = this.overlay.yRot = 0.0F;
        this.primary.zRot = this.overlay.zRot = 0.0F;
    }
}
