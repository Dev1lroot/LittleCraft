/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.content.item.ThighHighs;
import fr.dev1lroot.mcmods.littlecraft.model.ThighHighsModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

public class ThighHighsLayer<S extends HumanoidRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M>
{
    // Two model instances: one shows only the base parts, the other only the stripe parts.
    private final ThighHighsModel baseModel;
    private final ThighHighsModel stripeModel;

    public ThighHighsLayer(RenderLayerParent<S, M> parent, EntityModelSet modelSet)
    {
        super(parent);
        this.baseModel = new ThighHighsModel(modelSet.bakeLayer(ThighHighsModel.LAYER_LOCATION));
        this.baseModel.stripeRight.visible = false;
        this.baseModel.stripeLeft.visible  = false;

        this.stripeModel = new ThighHighsModel(modelSet.bakeLayer(ThighHighsModel.LAYER_LOCATION));
        this.stripeModel.baseRight.visible = false;
        this.stripeModel.baseLeft.visible  = false;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot)
    {
        // state.feetEquipment is populated because the ThighHighsItem's Equippable has an assetId,
        // which satisfies HumanoidMobRenderer.getEquipmentIfRenderable()'s shouldRender() check.
        ItemStack feet = state.feetEquipment;
        if (feet.isEmpty()) return;
        if (!(feet.getItem() instanceof ThighHighs.ThighHighsItem)) return;

        baseModel.setupAnim(state);
        stripeModel.setupAnim(state);

        // First pass: render base color using base texture.
        int baseColor = ThighHighs.getBaseColor(feet) | 0xFF000000;
        renderColoredCutoutModel(baseModel, ThighHighsModel.TEXTURE, pose, collector, light, state, baseColor, -1);

        // Second pass: render stripe color using stripe texture.
        int stripeColor = ThighHighs.getStripeColor(feet) | 0xFF000000;
        renderColoredCutoutModel(stripeModel, ThighHighsModel.TEXTURE_STRIPES, pose, collector, light, state, stripeColor, -1);
    }
}
