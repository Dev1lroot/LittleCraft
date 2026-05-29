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

public class PacifierModel extends EntityModel<HumanoidRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(Identifier.fromNamespaceAndPath(MODID, "pacifier_model"), "main");

    public static final Identifier TEXTURE_BODY =
        Identifier.fromNamespaceAndPath(MODID, "textures/models/armor/pacifier.png");
    public static final Identifier TEXTURE_RING =
        Identifier.fromNamespaceAndPath(MODID, "textures/models/armor/pacifier_ring.png");

    public final ModelPart body;
    public final ModelPart ring;

    public PacifierModel(ModelPart root)
    {
        super(root);
        this.body = root.getChild("body");
        this.ring = root.getChild("ring");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Nipple/shield — 4×4×2 disc sitting just in front of the face (Z = -4 is the front face plane).
        // Pivot at (0, 0, 0) = base of head; mouth is around Y = -2 to -4.
        root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, -4.0F, -5.5F, 4.0F, 4.0F, 1.5F, new CubeDeformation(0.0F)),
            PartPose.ZERO);

        // Guard ring — 6×6×1 flat ring/shield, slightly behind the body disc.
        root.addOrReplaceChild("ring",
            CubeListBuilder.create()
                .texOffs(0, 8)
                .addBox(-3.0F, -5.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.ZERO);

        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {}
}
