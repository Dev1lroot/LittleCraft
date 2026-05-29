/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.client.LittleKeys;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import fr.dev1lroot.mcmods.littlecraft.network.PissPacket;
import fr.dev1lroot.mcmods.littlecraft.network.PoopPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

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

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event)
    {
        // Diaper handles its own "Pooped" line in appendHoverText; skip it here
        if (event.getItemStack().getItem() instanceof Diaper.DiaperItem) return;

        if (Diaper.isPooped(event.getItemStack()))
            event.getToolTip().add(Component.translatable("item.littlecraft.diaper.pooped")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x8B4513))));
    }
}
