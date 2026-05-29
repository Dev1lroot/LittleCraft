/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.content.item.Pacifier;
import fr.dev1lroot.mcmods.littlecraft.model.PacifierModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

public class PacifierLayer<S extends HumanoidRenderState, M extends EntityModel<? super S>>
    extends RenderLayer<S, M>
{
    private final PacifierModel bodyModel;
    private final PacifierModel ringModel;

    public PacifierLayer(RenderLayerParent<S, M> parent, EntityModelSet modelSet)
    {
        super(parent);
        this.bodyModel = new PacifierModel(modelSet.bakeLayer(PacifierModel.LAYER_LOCATION));
        this.bodyModel.ring.visible = false;

        this.ringModel = new PacifierModel(modelSet.bakeLayer(PacifierModel.LAYER_LOCATION));
        this.ringModel.body.visible = false;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot)
    {
        ItemStack head = state.headEquipment;
        if (head.isEmpty()) return;
        if (!(head.getItem() instanceof Pacifier.PacifierItem)) return;

        if (!(getParentModel() instanceof HumanoidModel<?> humanoid)) return;
        syncHead(humanoid.head, bodyModel.body, ringModel.ring);

        int bodyColor = Pacifier.getBodyColor(head) | 0xFF000000;
        renderColoredCutoutModel(bodyModel, PacifierModel.TEXTURE_BODY, pose, collector, light, state, bodyColor, -1);

        int ringColor = Pacifier.getRingColor(head) | 0xFF000000;
        renderColoredCutoutModel(ringModel, PacifierModel.TEXTURE_RING, pose, collector, light, state, ringColor, -1);
    }

    private static void syncHead(ModelPart source, ModelPart body, ModelPart ring)
    {
        body.xRot = ring.xRot = source.xRot;
        body.yRot = ring.yRot = source.yRot;
        body.zRot = ring.zRot = source.zRot;
        body.x    = ring.x    = source.x;
        body.y    = ring.y    = source.y;
        body.z    = ring.z    = source.z;
    }
}
