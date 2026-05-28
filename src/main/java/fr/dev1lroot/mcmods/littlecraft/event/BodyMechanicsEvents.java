/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class BodyMechanicsEvents
{
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemUseAnimation anim = event.getItem().getItem().getUseAnimation(event.getItem());

        if (anim == ItemUseAnimation.EAT)
        {
            LittleData.addToStomach(player, 5);
            LittleData.addToBladder(player, 10);
        }
        else if (anim == ItemUseAnimation.DRINK)
        {
            LittleData.addToBladder(player, 250);
        }
    }
}
