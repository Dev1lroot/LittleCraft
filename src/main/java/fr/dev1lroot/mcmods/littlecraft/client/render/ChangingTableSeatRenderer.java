/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import fr.dev1lroot.mcmods.littlecraft.content.entity.ChangingTableSeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class ChangingTableSeatRenderer extends EntityRenderer<ChangingTableSeatEntity, EntityRenderState>
{
    public ChangingTableSeatRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState()
    {
        return new EntityRenderState();
    }
}
