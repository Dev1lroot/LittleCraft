/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
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
    // Cleared on resource pack reload so new designs are picked up without restart.
    private static final Map<String, Identifier[]> TEXTURE_CACHE = new HashMap<>();

    private final DiaperModel primaryModel;
    private final DiaperModel wetnessModel;

    public DiaperLayer(RenderLayerParent<S, M> parent, EntityModelSet modelSet)
    {
        super(parent);
        this.primaryModel = new DiaperModel(modelSet.bakeLayer(DiaperModel.LAYER_LOCATION));
        this.primaryModel.overlay.visible = false;

        this.wetnessModel = new DiaperModel(modelSet.bakeLayer(DiaperModel.LAYER_LOCATION));
        this.wetnessModel.primary.visible = false;
    }

    public static void clearCache()
    {
        TEXTURE_CACHE.clear();
    }

    private static Identifier[] resolveTextures(String design)
    {
        return TEXTURE_CACHE.computeIfAbsent(design, d -> {
            // Sanitize: only lowercase letters, digits, and underscores are valid
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
        // state.legsEquipment is populated because the diaper's Equippable has an assetId,
        // which satisfies HumanoidMobRenderer.getEquipmentIfRenderable()'s shouldRender() check.
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

        int damage = legs.getDamageValue();
        primaryModel.damage = damage;
        wetnessModel.damage = damage;

        primaryModel.setupAnim(state);
        wetnessModel.setupAnim(state);

        renderColoredCutoutModel(primaryModel, textures[0], pose, collector, light, state, -1, -1);

        if (damage > 0)
        {
            int alpha = Math.min(damage, 255);
            int color = (alpha << 24) | 0x00FFFFFF;
            collector.order(color)
                    .submitModel(
                            wetnessModel,
                            state,
                            pose,
                            RenderTypes.armorTranslucent(textures[1]),
                            light,
                            LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                            color,
                            null,
                            state.outlineColor,
                            null
                    );
        }
    }
}
