/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@EventBusSubscriber(modid = MODID)
public class CarryEventHandler
{
    // Tracks carriers currently holding a little so we can detect dismount transitions
    // and push the updated (empty) passenger list to their own client.
    private static final Set<UUID> activeCarriers = new HashSet<>();

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

        if (little.isPassenger() || carrier.isPassenger() || !carrier.getPassengers().isEmpty()) return;

        little.startRiding(carrier, true, true);

        // The entity tracker skips self-updates (player != this.entity guard in ChunkMap),
        // so the carrier's own client never receives ClientboundSetPassengersPacket via the
        // tracker. Send it directly so their client sets up the riding relationship immediately.
        if (carrier instanceof ServerPlayer serverCarrier) {
            serverCarrier.connection.send(new ClientboundSetPassengersPacket(carrier));
            activeCarriers.add(serverCarrier.getUUID());
        }

        carrier.sendSystemMessage(Component.translatable("littlecraft.carry.started.carrier", little.getName()));
        little.sendSystemMessage(Component.translatable("littlecraft.carry.started.little", carrier.getName()));

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer carrier)) return;
        if (carrier.level().isClientSide()) return;

        // Carrier pressing shift ejects the little passenger
        if (carrier.isShiftKeyDown()) {
            for (Entity passenger : List.copyOf(carrier.getPassengers())) {
                if (!(passenger instanceof Player little)) continue;
                int littleAge = LittleData.getAge(little);
                if (littleAge < 1 || littleAge > 12) continue;

                little.stopRiding();
                carrier.sendSystemMessage(Component.translatable("littlecraft.carry.stopped.carrier", little.getName()));
                little.sendSystemMessage(Component.translatable("littlecraft.carry.stopped.little", carrier.getName()));
            }
        }

        // Keep the carrier's own client in sync. The entity tracker never sends the carrier
        // their own entity's passenger changes, so we push the packet on every state transition.
        boolean isCarrying = carrier.getPassengers().stream()
            .anyMatch(p -> p instanceof Player lp
                && LittleData.getAge(lp) >= 1 && LittleData.getAge(lp) <= 12);

        UUID id = carrier.getUUID();
        boolean wasCarrying = activeCarriers.contains(id);

        if (isCarrying) {
            activeCarriers.add(id);
            carrier.connection.send(new ClientboundSetPassengersPacket(carrier));
        } else if (wasCarrying) {
            // Passenger dismounted (little pressed shift or another cause) — clear on carrier's client
            activeCarriers.remove(id);
            carrier.connection.send(new ClientboundSetPassengersPacket(carrier));
        }
    }
}
