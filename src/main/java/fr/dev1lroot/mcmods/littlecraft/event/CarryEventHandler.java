/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class CarryEventHandler
{
    // Carrier (age >= 18) right-clicks a little (1 <= age <= 12) with an empty main hand
    // → little player rides on carrier; carrier moves freely, little can only look and shift-dismount.
    // Carrier pressing shift also ejects the little.
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player carrier = event.getEntity();
        if (!(event.getTarget() instanceof Player little)) return;

        if (!carrier.getMainHandItem().isEmpty()) return;

        int carrierAge = LittleData.getAge(carrier);
        int littleAge  = LittleData.getAge(little);

        if (carrierAge < 18) return;
        if (littleAge < 1 || littleAge > 12) return;

        // Prevent stacking or double-mounting
        if (little.isPassenger() || carrier.isPassenger() || !carrier.getPassengers().isEmpty()) return;

        little.startRiding(carrier, true, true);

        carrier.sendSystemMessage(Component.translatable("littlecraft.carry.started.carrier", little.getName()));
        little.sendSystemMessage(Component.translatable("littlecraft.carry.started.little", carrier.getName()));

        event.setCanceled(true);
    }

    // When the carrier (vehicle) starts sneaking, eject any little passenger
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof Player carrier)) return;
        if (carrier.level().isClientSide()) return;
        if (!carrier.isShiftKeyDown()) return;
        if (carrier.getPassengers().isEmpty()) return;

        for (Entity passenger : List.copyOf(carrier.getPassengers()))
        {
            if (!(passenger instanceof Player little)) continue;
            int littleAge = LittleData.getAge(little);
            if (littleAge < 1 || littleAge > 12) continue;

            little.stopRiding();
            carrier.sendSystemMessage(Component.translatable("littlecraft.carry.stopped.carrier", little.getName()));
            little.sendSystemMessage(Component.translatable("littlecraft.carry.stopped.little", carrier.getName()));
        }
    }
}
