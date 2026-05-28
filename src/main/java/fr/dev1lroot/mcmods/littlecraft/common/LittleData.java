/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.common;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@Mod(MODID)
public class LittleData
{
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> LITTLEAGE =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Integer>>) ATTACHMENT_TYPES.register("little_age", () ->
            AttachmentType.builder(() -> 25)
                .serialize(Codec.INT.optionalFieldOf("value", 25))
                .copyOnDeath()
                .sync(ByteBufCodecs.VAR_INT)
                .build());

    public LittleData(IEventBus modBus)
    {
        ATTACHMENT_TYPES.register(modBus);
    }

    public static boolean isLittle(Player player)
    {
        return getAge(player) > 0;
    }

    public static void setAge(Player player, int value)
    {
        player.setData(LITTLEAGE.get(), value);
    }

    public static int getAge(Player player)
    {
        return player.getData(LITTLEAGE.get());
    }

    // Returns [0,1] blend factor: 0 = full little scale, 1 = full adult scale
    public static float getAgeBlend(int age)
    {
        if (age <= 6) return 0.0F;
        if (age >= 18) return 1.0F;
        return (age - 6) / 12.0F;
    }

    // Body/model scale for a little player of the given age
    public static float computeBodyScale(int age)
    {
        return 0.5F + 0.5F * getAgeBlend(age);
    }

    // Head scale (relative to body) to keep head visually proportional
    public static float computeHeadScale(int age)
    {
        return 2.0F - getAgeBlend(age);
    }
}
