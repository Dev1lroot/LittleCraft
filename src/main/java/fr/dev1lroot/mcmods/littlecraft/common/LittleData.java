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

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> BLADDER =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Integer>>) ATTACHMENT_TYPES.register("bladder", () ->
            AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.optionalFieldOf("value", 0))
                .copyOnDeath()
                .sync(ByteBufCodecs.VAR_INT)
                .build());

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> STOMACH =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Integer>>) ATTACHMENT_TYPES.register("stomach", () ->
            AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.optionalFieldOf("value", 0))
                .copyOnDeath()
                .sync(ByteBufCodecs.VAR_INT)
                .build());

    // Unbounded buffer: solid food eaten, drains into STOMACH at 1 g per 20 ticks.
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> EATEN =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Integer>>) ATTACHMENT_TYPES.register("eaten", () ->
            AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.optionalFieldOf("value", 0))
                .copyOnDeath()
                .sync(ByteBufCodecs.VAR_INT)
                .build());

    // Unbounded buffer: liquid consumed, drains into BLADDER at 1 ml per 5 ticks.
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> DRINKED =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Integer>>) ATTACHMENT_TYPES.register("drinked", () ->
            AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.optionalFieldOf("value", 0))
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

    public static int getBladder(Player player)
    {
        return player.getData(BLADDER.get());
    }

    public static void setBladder(Player player, int value)
    {
        player.setData(BLADDER.get(), Math.max(0, Math.min(value, computeBladderCapacity(getAge(player)))));
    }

    public static void addToBladder(Player player, int amount)
    {
        setBladder(player, getBladder(player) + amount);
    }

    public static int getStomach(Player player)
    {
        return player.getData(STOMACH.get());
    }

    public static void setStomach(Player player, int value)
    {
        player.setData(STOMACH.get(), Math.max(0, Math.min(value, computeStomachCapacity(getAge(player)))));
    }

    public static void addToStomach(Player player, int amount)
    {
        setStomach(player, getStomach(player) + amount);
    }

    public static int computeBladderCapacity(int age)
    {
        return Math.max(200, Math.min(2000, age * 80));
    }

    public static int computeStomachCapacity(int age)
    {
        return Math.max(100, Math.min(1000, age * 40));
    }

    // Returns [0,1] blend factor: 0 = full little scale, 1 = full adult scale
    public static float getAgeBlend(int age)
    {
        if (age <= 6) return 0.0F;
        if (age >= 18) return 1.0F;
        return (age - 6) / 12.0F;
    }

    public static int getEaten(Player player)
    {
        return player.getData(EATEN.get());
    }

    public static void setEaten(Player player, int value)
    {
        player.setData(EATEN.get(), Math.max(0, value));
    }

    public static void addToEaten(Player player, int amount)
    {
        setEaten(player, getEaten(player) + amount);
    }

    public static int getDrinked(Player player)
    {
        return player.getData(DRINKED.get());
    }

    public static void setDrinked(Player player, int value)
    {
        player.setData(DRINKED.get(), Math.max(0, value));
    }

    public static void addToDrinked(Player player, int amount)
    {
        setDrinked(player, getDrinked(player) + amount);
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
