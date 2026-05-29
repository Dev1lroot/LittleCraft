/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client;

import fr.dev1lroot.mcmods.littlecraft.client.color.PacifierBodyColorTint;
import fr.dev1lroot.mcmods.littlecraft.client.color.PacifierRingColorTint;
import fr.dev1lroot.mcmods.littlecraft.client.color.ThighHighsBaseColorTint;
import fr.dev1lroot.mcmods.littlecraft.client.color.ThighHighsStripeColorTint;
import fr.dev1lroot.mcmods.littlecraft.client.item.DiaperDesignProperty;
import fr.dev1lroot.mcmods.littlecraft.client.item.DiaperFillProperty;
import fr.dev1lroot.mcmods.littlecraft.client.render.BodyStatsHud;
import fr.dev1lroot.mcmods.littlecraft.client.render.CribRenderer;
import fr.dev1lroot.mcmods.littlecraft.client.render.DiaperLayer;
import fr.dev1lroot.mcmods.littlecraft.client.render.PottySeatRenderer;
import fr.dev1lroot.mcmods.littlecraft.client.render.ThighHighsLayer;
import fr.dev1lroot.mcmods.littlecraft.content.Crib;
import fr.dev1lroot.mcmods.littlecraft.content.Potty;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import fr.dev1lroot.mcmods.littlecraft.model.DiaperModel;
import fr.dev1lroot.mcmods.littlecraft.model.ThighHighsModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@Mod(value = MODID, dist = Dist.CLIENT)
public class ClientRegistry
{
    public ClientRegistry(IEventBus modEventBus)
    {
        modEventBus.addListener(ClientRegistry::onRegisterLayerDefinitions);
        modEventBus.addListener(ClientRegistry::onAddLayers);
        modEventBus.addListener(ClientRegistry::onRegisterBlockEntityRenderers);
        modEventBus.addListener(ClientRegistry::onAddClientReloadListeners);
        modEventBus.addListener(ClientRegistry::onRegisterItemTintSources);
        modEventBus.addListener(ClientRegistry::onRegisterGuiLayers);
        modEventBus.addListener(ClientRegistry::onRegisterKeyMappings);
        modEventBus.addListener(ClientRegistry::onRegisterSelectItemModelProperties);
        modEventBus.addListener(ClientRegistry::onRegisterRangeSelectItemModelProperties);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(DiaperModel.LAYER_LOCATION, DiaperModel::createBodyLayer);
        event.registerLayerDefinition(ThighHighsModel.LAYER_LOCATION, ThighHighsModel::createBodyLayer);
    }

    private static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(Crib.CRIB_BLOCK_ENTITY.get(), CribRenderer::new);
        event.registerEntityRenderer(Potty.POTTY_SEAT.get(), PottySeatRenderer::new);
    }

    @SuppressWarnings("unchecked")
    private static void onAddLayers(EntityRenderersEvent.AddLayers event)
    {
        EntityModelSet modelSet = event.getEntityModels();

        for (PlayerModelType type : PlayerModelType.values())
        {
            var renderer = event.getPlayerRenderer(type);
            if (renderer != null)
            {
                renderer.addLayer(new DiaperLayer<>(renderer, modelSet));
                renderer.addLayer(new ThighHighsLayer<>(renderer, modelSet));
            }
        }

        var armorStandRenderer = (LivingEntityRenderer) event.<net.minecraft.world.entity.decoration.ArmorStand, LivingEntityRenderer>getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null)
        {
            armorStandRenderer.addLayer(new DiaperLayer<>(armorStandRenderer, modelSet));
            armorStandRenderer.addLayer(new ThighHighsLayer<>(armorStandRenderer, modelSet));
        }
    }

    private static void onAddClientReloadListeners(AddClientReloadListenersEvent event)
    {
        event.addListener(
            Identifier.fromNamespaceAndPath(MODID, "diaper_texture_cache"),
            (ResourceManagerReloadListener) rm -> DiaperLayer.clearCache()
        );
    }

    private static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event)
    {
        event.register(
            Identifier.fromNamespaceAndPath(MODID, "thigh_highs_base_color"),
            ThighHighsBaseColorTint.MAP_CODEC
        );
        event.register(
            Identifier.fromNamespaceAndPath(MODID, "thigh_highs_stripe_color"),
            ThighHighsStripeColorTint.MAP_CODEC
        );
        event.register(
            Identifier.fromNamespaceAndPath(MODID, "pacifier_body_color"),
            PacifierBodyColorTint.MAP_CODEC
        );
        event.register(
            Identifier.fromNamespaceAndPath(MODID, "pacifier_ring_color"),
            PacifierRingColorTint.MAP_CODEC
        );
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(
            Identifier.fromNamespaceAndPath(MODID, "body_stats"),
            new BodyStatsHud()
        );
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(LittleKeys.KEY_PISS);
        event.register(LittleKeys.KEY_POOP);
    }

    private static void onRegisterSelectItemModelProperties(RegisterSelectItemModelPropertyEvent event)
    {
        event.register(Identifier.fromNamespaceAndPath(MODID, "diaper_design"), DiaperDesignProperty.TYPE);
    }

    private static void onRegisterRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event)
    {
        event.register(Identifier.fromNamespaceAndPath(MODID, "diaper_fill"), DiaperFillProperty.MAP_CODEC);
    }
}
