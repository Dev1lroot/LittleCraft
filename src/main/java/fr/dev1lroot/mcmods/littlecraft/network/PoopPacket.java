/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.network;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.LittleMobEffects;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public record PoopPacket() implements CustomPacketPayload
{
    public static final Type<PoopPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MODID, "poop"));
    public static final StreamCodec<ByteBuf, PoopPacket> STREAM_CODEC = StreamCodec.unit(new PoopPacket());

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(PoopPacket packet, IPayloadContext context)
    {
        Player player = context.player();

        ItemStack diaper = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!(diaper.getItem() instanceof Diaper.DiaperItem)) return;

        int stomach = LittleData.getStomach(player);
        if (stomach < 100) return;

        int drop    = Math.min(stomach, player.getRandom().nextIntBetweenInclusive(100, 250));
        int used    = Diaper.getUsed(diaper);

        ItemStack updated = Diaper.setPooped(diaper, true);
        updated = Diaper.setUsed(updated, used + drop);
        LittleData.setStomach(player, stomach - drop);

        player.setItemSlot(EquipmentSlot.LEGS, updated);
        player.addEffect(new MobEffectInstance(LittleMobEffects.STINK, MobEffectInstance.INFINITE_DURATION, 0));
    }
}
