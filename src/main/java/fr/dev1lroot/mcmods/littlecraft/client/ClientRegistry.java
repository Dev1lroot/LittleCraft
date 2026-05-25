package fr.dev1lroot.mcmods.littlecraft.client;

import fr.dev1lroot.mcmods.littlecraft.client.render.DiaperLayer;
import fr.dev1lroot.mcmods.littlecraft.model.DiaperModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@Mod(value = MODID, dist = Dist.CLIENT)
public class ClientRegistry
{
    public ClientRegistry(IEventBus modEventBus)
    {
        modEventBus.addListener(ClientRegistry::onRegisterLayerDefinitions);
        modEventBus.addListener(ClientRegistry::onAddLayers);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(DiaperModel.LAYER_LOCATION, DiaperModel::createBodyLayer);
    }

    @SuppressWarnings("unchecked")
    private static void onAddLayers(EntityRenderersEvent.AddLayers event)
    {
        EntityModelSet modelSet = event.getEntityModels();

        // Players (both WIDE and SLIM skin types)
        for (PlayerModelType type : PlayerModelType.values())
        {
            var renderer = event.getPlayerRenderer(type);
            if (renderer != null)
            {
                renderer.addLayer(new DiaperLayer<>(renderer, modelSet));
            }
        }

        // Armor stands
        var armorStandRenderer = (LivingEntityRenderer) event.<net.minecraft.world.entity.decoration.ArmorStand, LivingEntityRenderer>getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null)
        {
            armorStandRenderer.addLayer(new DiaperLayer<>(armorStandRenderer, modelSet));
        }
    }
}
