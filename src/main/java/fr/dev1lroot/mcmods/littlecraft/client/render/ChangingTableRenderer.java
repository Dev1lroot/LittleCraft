/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.content.block.ChangingTableBlockEntity;
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

public class ChangingTableRenderer implements BlockEntityRenderer<ChangingTableBlockEntity, ChangingTableRenderState>
{
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public ChangingTableRenderer(BlockEntityRendererProvider.Context context)
    {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public ChangingTableRenderState createRenderState() { return new ChangingTableRenderState(); }

    @Override
    public void extractRenderState(
        ChangingTableBlockEntity blockEntity,
        ChangingTableRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        blockModelResolver.update(state.blockModel, blockEntity.getBlockState(), DISPLAY_CONTEXT);
    }

    @Override
    public void submit(
        ChangingTableRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera)
    {
        state.blockModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }
}
