/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.dev1lroot.mcmods.littlecraft.client.TextureCompositor;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import fr.dev1lroot.mcmods.littlecraft.model.DiaperModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class DiaperLayer<S extends HumanoidRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M>
{
    // Caches resolved [primary, wetness] texture pairs per design name.
    private static final Map<String, Identifier[]> TEXTURE_CACHE = new HashMap<>();

    // diaper.json — default dry shape
    private final DiaperModel modelDry;
    // diaper_full.json — capacity >= 5000 OR used >= capacity/2
    private final DiaperModel modelFull;
    // diaper_flooded.json — used >= capacity/5 OR capacity >= 7000
    private final DiaperModel modelFlooded;

    public DiaperLayer(RenderLayerParent<S, M> parent, EntityModelSet modelSet)
    {
        super(parent);
        var rm = Minecraft.getInstance().getResourceManager();
        this.modelDry     = new DiaperModel(rm, DiaperModel.MODEL_DRY,     0.0f);
        this.modelFull    = new DiaperModel(rm, DiaperModel.MODEL_FULL,    0.0f);
        this.modelFlooded = new DiaperModel(rm, DiaperModel.MODEL_FLOODED, 0.0f);
    }

    public static void clearCache()
    {
        TEXTURE_CACHE.clear();
        TextureCompositor.clearAll();
    }

    private static Identifier[] resolveTextures(String design)
    {
        return TEXTURE_CACHE.computeIfAbsent(design, d -> {
            String safe = d.replaceAll("[^a-z0-9_]", "");
            if (safe.isEmpty()) safe = "default";

            Identifier primary = Identifier.fromNamespaceAndPath(MODID, "textures/diaper/" + safe + "/default.png");
            Identifier wetness = Identifier.fromNamespaceAndPath(MODID, "textures/diaper/" + safe + "/wetness.png");

            var rm = Minecraft.getInstance().getResourceManager();
            return new Identifier[]{
                rm.getResource(primary).isPresent() ? primary : DiaperModel.TEXTURE_PRIMARY,
                rm.getResource(wetness).isPresent() ? wetness : DiaperModel.TEXTURE_WETNESS
            };
        });
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot)
    {
        ItemStack legs = state.legsEquipment;
        if (legs.isEmpty()) return;
        if (!(legs.getItem() instanceof Diaper.DiaperItem)) return;

        String design = "default";
        CustomData customData = legs.get(DataComponents.CUSTOM_DATA);
        if (customData != null)
        {
            String raw = customData.copyTag().getStringOr("DESIGN", "");
            if (!raw.isEmpty()) design = raw;
        }

        Identifier[] textures = resolveTextures(design);
        int used     = Diaper.getUsed(legs);
        int capacity = Diaper.getCapacity(legs);

        // flooded: used >= capacity/5 OR capacity >= 7000 (takes priority)
        // full:    capacity >= 5000 OR used >= capacity/2
        boolean isFlooded = used * 5 >= capacity || capacity >= 7000;
        boolean isFull    = capacity >= 5000 || used * 2 >= capacity;
        DiaperModel model = isFlooded ? modelFlooded : (isFull ? modelFull : modelDry);

        // Composite wetness into the base texture on the CPU; use plain base when dry.
        // Bucket alpha to steps of 4 (64 levels) before compiling to limit cache size.
        Identifier texture = textures[0];
        if (used > 0 && Diaper.isPeed(legs))
        {
            int rawAlpha = Math.min(255, Math.round(255f * used / capacity));
            int alpha    = (rawAlpha >> 2) << 2;
            texture = new TextureCompositor()
                    .addLayer(textures[0])
                    .addLayer(textures[1], alpha)
                    .compile();
        }

        model.setupAnim(state);
        pose.pushPose();
        pose.scale(1.25f, 1.25f, 1.25f);
        collector.order(-1)
                .submitModel(
                        model,
                        state,
                        pose,
                        RenderTypes.entityTranslucent(texture),
                        light,
                        LivingEntityRenderer.getOverlayCoords(state, 0.0f),
                        -1,
                        null,
                        state.outlineColor,
                        null
                );
        pose.popPose();
    }
}
