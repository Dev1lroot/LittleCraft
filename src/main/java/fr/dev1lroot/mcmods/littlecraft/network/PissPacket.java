/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.network;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.LittleMobEffects;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlockEntity;
import fr.dev1lroot.mcmods.littlecraft.content.entity.PottySeatEntity;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public record PissPacket() implements CustomPacketPayload
{
    public static final Type<PissPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID, "piss"));
    public static final StreamCodec<ByteBuf, PissPacket> STREAM_CODEC = StreamCodec.unit(new PissPacket());

    // Stores the server tickCount at which each player last sent a PissPacket.
    // Used by PlayerTickMixin to suppress passive fill while the key is held.
    static final Map<UUID, Integer> lastPissTick = new HashMap<>();

    // 10 ticks = 2x the client send rate (5 ticks), giving one missed packet of tolerance.
    public static final int ACTIVE_WINDOW = 10;

    public static boolean isActivePissing(Player player)
    {
        Integer last = lastPissTick.get(player.getUUID());
        return last != null && (player.tickCount - last) < ACTIVE_WINDOW;
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(PissPacket packet, IPayloadContext context)
    {
        Player player = context.player();
        lastPissTick.put(player.getUUID(), player.tickCount);

        /*
            The packet is sent every 5 ticks while the key is held.
            5 ticks = 1/4 s → average pee speed 24 ml/s → 6 ml per packet.
        */
        int bladder = LittleData.getBladder(player);
        performPiss(player, Math.min(bladder, 6));
    }

    /**
     * Routes a piss of the given amount through the same priority chain as the packet:
     * diaper → other equippable pants (shame) → potty → bare floor (shame).
     * Safe to call from any server-side context (incontinence, packet handler, etc.).
     */
    public static void performPiss(Player player, int amount)
    {
        if (amount <= 0) return;

        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);

        // Diaper: highest priority, no shame.
        if (legs.getItem() instanceof Diaper.DiaperItem)
        {
            int used     = Diaper.getUsed(legs);
            int capacity = Diaper.getCapacity(legs);
            if (used < capacity)
            {
                int bladder = LittleData.getBladder(player);
                int actual  = Math.min(amount, bladder);
                player.setItemSlot(EquipmentSlot.LEGS, Diaper.setUsed(legs, used + actual));
                LittleData.setBladder(player, bladder - actual);
                return;
            }
        }
        // Other equippable pants (not a diaper): piss in pants, apply shame.
        else
        {
            Equippable equippable = legs.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == EquipmentSlot.LEGS)
            {
                int bladder = LittleData.getBladder(player);
                if (bladder > 0)
                {
                    LittleData.setBladder(player, bladder - Math.min(amount, bladder));
                    player.addEffect(new MobEffectInstance(LittleMobEffects.SHAME, 600, 0));
                }
                return;
            }
        }

        // No legs item or diaper full — try potty next.
        if (player.getVehicle() instanceof PottySeatEntity seat)
        {
            PottyBlockEntity potty = seat.getPottyBlockEntity();
            if (potty != null && !potty.isPissFull())
            {
                int bladder = LittleData.getBladder(player);
                if (bladder > 0)
                {
                    int actual = Math.min(amount, bladder);
                    potty.addPiss(actual);
                    LittleData.setBladder(player, bladder - actual);
                }
            }
            return;
        }

        // No diaper, no pants, no potty — piss on the floor.
        int bladder = LittleData.getBladder(player);
        if (bladder > 0)
        {
            LittleData.setBladder(player, bladder - Math.min(amount, bladder));
            player.addEffect(new MobEffectInstance(LittleMobEffects.SHAME, 600, 0));
        }
    }
}
