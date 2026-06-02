/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.common.LittleData;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.recipe.BabyBottleFillRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Locale;
import java.util.function.Consumer;

public class BabyBottle
{
    public static final int SIP_AMOUNT  = 50;
    public static final int FULL_AMOUNT = 500;

    public static class BabyBottleItem extends Item
    {
        public BabyBottleItem(Properties properties)
        {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand)
        {
            ItemStack stack = player.getItemInHand(hand);
            if (getFluidAmount(stack) > 0)
            {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
        {
            int amount = getFluidAmount(stack);
            if (amount <= 0) return stack;

            int sipped = Math.min(SIP_AMOUNT, amount);
            ItemStack result = setFluidAmount(stack, amount - sipped);

            if (!level.isClientSide() && entity instanceof Player player)
                LittleData.addToDrinked(player, sipped);

            return result;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity)
        {
            return 32;
        }

        @Override
        public ItemUseAnimation getUseAnimation(ItemStack stack)
        {
            return ItemUseAnimation.DRINK;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                    Consumer<Component> consumer, TooltipFlag flag)
        {
            super.appendHoverText(stack, context, display, consumer, flag);

            String fluidName   = getFluidName(stack);
            int    fluidAmount = getFluidAmount(stack);

            Component fluidLabel = Component.translatable("item.littlecraft.baby_bottle.fluid")
                    .append(Component.literal(" "));

            if (!fluidName.isEmpty())
                consumer.accept(fluidLabel.copy()
                        .append(Component.translatable(fluidName))
                        .withStyle(ChatFormatting.GRAY));
            else
                consumer.accept(fluidLabel.copy()
                        .append(Component.translatable("item.littlecraft.baby_bottle.fluid.empty"))
                        .withStyle(ChatFormatting.GRAY));

            consumer.accept(
                    Component.translatable("item.littlecraft.baby_bottle.amount")
                            .append(Component.literal(" " + String.format(Locale.ENGLISH, "%,d", fluidAmount) + "ml"))
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    public static String getFluidName(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.copyTag().getStringOr("fluid_name", "");
        return "";
    }

    public static int getFluidAmount(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.copyTag().getIntOr("fluid_amount", 0);
        return 0;
    }

    public static ItemStack setFluid(ItemStack stack, String fluidName, int amount)
    {
        ItemStack result = stack.copy();
        CustomData existing = result.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putString("fluid_name", fluidName);
        tag.putInt("fluid_amount", Math.max(0, amount));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return result;
    }

    private static ItemStack setFluidAmount(ItemStack stack, int amount)
    {
        ItemStack result = stack.copy();
        CustomData existing = result.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt("fluid_amount", Math.max(0, amount));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return result;
    }

    private static CustomData defaultData()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("fluid_name", "");
        tag.putInt("fluid_amount", 0);
        return CustomData.of(tag);
    }

    public static final DeferredItem<BabyBottleItem> BABY_BOTTLE =
            LittleContentRegistry.ITEMS.registerItem("baby_bottle",
                    props -> new BabyBottleItem(props
                            .stacksTo(1)
                            .component(DataComponents.CUSTOM_DATA, defaultData())
                    ));

    public static void register()
    {
        BabyBottleFillRecipe.registerSerializer();
    }
}
