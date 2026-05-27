/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.content.block.CribBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/*
    CribRenderer
    ----------------
    Renders the placed crib block entity using the blockstate JSON model system.
    Each half (HEAD / FOOT) is a separate CribBlockEntity; this renderer resolves
    the block entity's own BlockState against the blockstate variants in
    assets/littlecraft/blockstates/crib.json and submits the result as a standard
    block model — no LayerDefinition, no sprite atlas, no model layers.

    Model geometry lives in:
      assets/littlecraft/models/block/crib_head.json
      assets/littlecraft/models/block/crib_foot.json

    To change geometry or textures, edit those JSON files.
    To change the block-entity rendering pipeline, edit this class.
*/
public class CribRenderer implements BlockEntityRenderer<CribBlockEntity, CribRenderState>
{
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public CribRenderer(BlockEntityRendererProvider.Context context)
    {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public CribRenderState createRenderState()
    {
        return new CribRenderState();
    }

    @Override
    public void extractRenderState(
        CribBlockEntity blockEntity,
        CribRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        blockModelResolver.update(state.blockModel, blockEntity.getBlockState(), DISPLAY_CONTEXT);
    }

    @Override
    public void submit(
        CribRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera)
    {
        state.blockModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }
}
