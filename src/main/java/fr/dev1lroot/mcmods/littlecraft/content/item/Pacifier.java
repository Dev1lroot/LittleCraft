/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.recipe.PacifierDyeRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Consumer;


public class Pacifier
{
    public static class PacifierItem extends Item
    {
        public PacifierItem(Properties properties)
        {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                     Consumer<Component> consumer, TooltipFlag flag)
        {
            super.appendHoverText(stack, context, display, consumer, flag);

            int bodyColor = Pacifier.getBodyColor(stack);
            int ringColor = Pacifier.getRingColor(stack);

            consumer.accept(
                Component.translatable("item.littlecraft.pacifier.body_color")
                    .append(Component.literal(" ■").withStyle(s -> s.withColor(bodyColor)))
                    .append(Component.literal(String.format(" #%06X", bodyColor)))
                    .withStyle(ChatFormatting.GRAY)
            );
            consumer.accept(
                Component.translatable("item.littlecraft.pacifier.ring_color")
                    .append(Component.literal(" ■").withStyle(s -> s.withColor(ringColor)))
                    .append(Component.literal(String.format(" #%06X", ringColor)))
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static CustomData defaultData()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("body_color", 0xFFFFFF);
        tag.putInt("ring_color", 0xFFFFFF);
        return CustomData.of(tag);
    }

    public static int getBodyColor(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.copyTag().getIntOr("body_color", 0xFFFFFF);
        return 0xFFFFFF;
    }

    public static int getRingColor(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.copyTag().getIntOr("ring_color", 0xFFFFFF);
        return 0xFFFFFF;
    }

    public static ItemStack setColors(ItemStack stack, int bodyColor, int ringColor)
    {
        ItemStack result = stack.copy();
        CustomData existing = result.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt("body_color", bodyColor);
        tag.putInt("ring_color", ringColor);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return result;
    }

    public static int mixColor(int existing, int newDye)
    {
        return ThighHighs.mixColor(existing, newDye);
    }

    public static final DeferredItem<PacifierItem> PACIFIER =
        LittleContentRegistry.ITEMS.registerItem("pacifier",
            props -> new PacifierItem(props
                .durability(65)
                .enchantable(15)
                .repairable(LittleMaterials.FABRIC_TAG)
                .component(DataComponents.CUSTOM_DATA, defaultData())
                .component(DataComponents.EQUIPPABLE,
                    Equippable.builder(EquipmentSlot.HEAD)
                        .build())
            ));

    public static void register()
    {
        PacifierDyeRecipe.registerSerializer();
    }
}
