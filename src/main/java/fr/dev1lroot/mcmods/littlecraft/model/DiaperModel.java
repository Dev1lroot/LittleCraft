/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
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
    public final ModelPart primary_inner;
    public final ModelPart overlay_inner;

    public DiaperModel(ModelPart root)
    {
        super(root);

        // Base diaper box    
        this.primary = root.getChild("primary");
        this.overlay = root.getChild("overlay");

        // Internal safeguards
        this.primary_inner = root.getChild("primary_inner");
        this.overlay_inner = root.getChild("overlay_inner");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("primary",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("overlay",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("primary_inner",
                CubeListBuilder.create()
                        .texOffs(0, 11)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("overlay_inner",
                CubeListBuilder.create()
                        .texOffs(0, 11)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F),
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
            this.primary.y = this.overlay.y = this.primary_inner.y = this.overlay_inner.y = 9.0F;
            this.primary.z = this.overlay.z = this.primary_inner.z = this.overlay_inner.z = 3.0F;
            this.primary.xRot = this.overlay.xRot = this.primary_inner.xRot = this.overlay_inner.xRot = 0.5F;
        }
        // Swimming and sleeping rotations are handled by the entity renderer's setupRotations;
        // the diaper inherits those poseStack transforms automatically as a RenderLayer.
    }

    private void reset()
    {
        // y=8 places the box at model y=8–16 (the hip joint of a standard player model).
        this.primary.y = this.overlay.y = this.primary_inner.y = this.overlay_inner.y = 8.0F;
        this.primary.z = this.overlay.z = this.primary_inner.z = this.overlay_inner.z = 0.0F;
        this.primary.x = this.overlay.x = this.primary_inner.x = this.overlay_inner.x = 0.0F;

        this.primary.xRot = this.overlay.xRot = this.primary_inner.xRot = this.overlay_inner.xRot = 0.0F;
        this.primary.yRot = this.overlay.yRot = this.primary_inner.yRot = this.overlay_inner.yRot = 0.0F;
        this.primary.zRot = this.overlay.zRot = this.primary_inner.zRot = this.overlay_inner.zRot = 0.0F;

        this.primary.xScale = this.primary.yScale = this.primary.zScale =
        this.primary_inner.xScale = this.primary_inner.yScale = this.primary_inner.zScale   = 1.2F;

        this.overlay.xScale = this.overlay.yScale = this.overlay.zScale =
        this.overlay_inner.xScale = this.overlay_inner.yScale = this.overlay_inner.zScale   = 1.21F;
    }
}
