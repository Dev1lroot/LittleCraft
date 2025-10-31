package fr.dev1lroot.mcmods.littlecraft.client;

import fr.dev1lroot.mcmods.littlecraft.model.DiaperModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientRegistry
{
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(DiaperModel.LAYER_LOCATION, DiaperModel::createBodyLayer);
    }
}