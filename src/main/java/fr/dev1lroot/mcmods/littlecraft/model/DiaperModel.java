/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.model;

import com.dev1lroot.mclib.formabitur.Formabitur;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

/**
 * Entity model for the worn diaper, driven entirely by {@code models/block/diaper.json}.
 * Geometry is loaded at construction time via {@link Formabitur}; edit the JSON in
 *
 * Two instances are used by {@link fr.dev1lroot.mcmods.littlecraft.client.render.DiaperLayer}:
 *  • inflate = 0.0  → primary (dry) layer
 *  • inflate = 0.01 → wetness overlay (slightly larger to avoid Z-fighting)
 *
 * Texture identifiers remain as constants so other code can reference them.
 */
public class DiaperModel extends EntityModel<HumanoidRenderState>
{
    public static final Identifier TEXTURE_PRIMARY = Identifier.fromNamespaceAndPath(MODID, "textures/diaper/default/default.png");
    public static final Identifier TEXTURE_WETNESS = Identifier.fromNamespaceAndPath(MODID, "textures/diaper/default/wetness.png");

    public static final Identifier MODEL_DRY     =
        Identifier.fromNamespaceAndPath(MODID, "models/block/diaper.json");
    public static final Identifier MODEL_FULL    =
        Identifier.fromNamespaceAndPath(MODID, "models/block/diaper_full.json");
    public static final Identifier MODEL_FLOODED =
        Identifier.fromNamespaceAndPath(MODID, "models/block/diaper_flooded.json");

    private static final float TEX_WIDTH  = 32f;
    private static final float TEX_HEIGHT = 32f;

    // Base placement offset (entity-model units, 1 unit = 1 voxel).
    private static final float OFFSET_X   =  8.0f;  // right
    private static final float OFFSET_Z   =  -8.0f;  // front
    private static final float OFFSET_Y   =  14.0f;  // down - tune to taste

    // Crouching offsets in entity-model units.
    private static final float CROUCH_Y   =  2.0f;
    private static final float CROUCH_Z   =  6.0f;
    private static final float CROUCH_ROT =  0.5f;

    /** The root part is also the sole animated part (no sub-hierarchy). */
    private final ModelPart root;

    public DiaperModel(ResourceManager rm, Identifier modelPath, float inflate)
    {
        super(Formabitur.buildFromBlockModel(rm, modelPath, TEX_WIDTH, TEX_HEIGHT, inflate));
        this.root = root();
    }

    @Override
    public void setupAnim(HumanoidRenderState state)
    {
        root.x      = OFFSET_X;
        root.y      = OFFSET_Y;
        root.z      = OFFSET_Z;
        root.xRot   = 0f;
        root.zRot   = 0f;
        root.xScale = -1f;
        root.yScale = -1f;

        if (state.isCrouching)
        {
            root.y    += CROUCH_Y;
            root.z    += CROUCH_Z;
            root.xRot  = CROUCH_ROT;
        }
    }
}
