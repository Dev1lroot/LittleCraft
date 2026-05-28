/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.client.LittleKeys;
import fr.dev1lroot.mcmods.littlecraft.network.PissPacket;
import fr.dev1lroot.mcmods.littlecraft.network.PoopPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class BodyMechanicsClientEvents
{
    private static int pissTick = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (LittleKeys.KEY_PISS.isDown())
        {
            pissTick++;
            if (pissTick >= 5)
            {
                pissTick = 0;
                ClientPacketDistributor.sendToServer(new PissPacket());
            }
        }
        else
        {
            pissTick = 0;
        }

        while (LittleKeys.KEY_POOP.consumeClick())
            ClientPacketDistributor.sendToServer(new PoopPacket());
    }
}
