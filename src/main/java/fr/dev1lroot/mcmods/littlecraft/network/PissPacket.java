/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.network;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

        // TODO: Bad effects for diaperless use of this function;
        
        ItemStack diaper = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!(diaper.getItem() instanceof Diaper.DiaperItem)) return;


        /*
            the packet calls each 5 ticks when key is hold,
            5 ticks is 1/4 of the 1 second,
            average peeing speed of human is 15ml/s (male) and 30ml/s (female),
            I will use 24ml/s
            24 / 4 = 6;
        */

        int used     = Diaper.getUsed(diaper);
        int capacity = Diaper.getCapacity(diaper);
        int bladder  = LittleData.getBladder(player);

        if (bladder > 0)
        {
            int amount = Math.min(bladder,6); // 6 per 5 ticks (24 per second) but only when exists;
            player.setItemSlot(EquipmentSlot.LEGS, Diaper.setUsed(diaper, used + amount));
            LittleData.setBladder(player, bladder - amount);
        }
    }
}
