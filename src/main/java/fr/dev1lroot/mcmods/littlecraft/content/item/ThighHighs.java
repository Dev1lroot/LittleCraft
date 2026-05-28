/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.recipe.ThighHighsDyeRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Consumer;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class ThighHighs
{
    public static class ThighHighsItem extends Item
    {
        public ThighHighsItem(Properties properties)
        {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag)
        {
            super.appendHoverText(stack, context, display, consumer, flag);

            int baseColor   = ThighHighs.getBaseColor(stack);
            int stripeColor = ThighHighs.getStripeColor(stack);

            consumer.accept(
                Component.translatable("item.littlecraft.thigh_highs.base_color")
                    .append(Component.literal(" ■").withStyle(s -> s.withColor(baseColor)))
                    .append(Component.literal(String.format(" #%06X", baseColor)))
                    .withStyle(ChatFormatting.GRAY)
            );
            consumer.accept(
                Component.translatable("item.littlecraft.thigh_highs.stripe_color")
                    .append(Component.literal(" ■").withStyle(s -> s.withColor(stripeColor)))
                    .append(Component.literal(String.format(" #%06X", stripeColor)))
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    // Equipment asset key pointing to assets/littlecraft/equipment/thigh_highs.json.
    // The JSON has no layers, so HumanoidArmorLayer renders nothing — our ThighHighsLayer does all rendering.
    @SuppressWarnings("unchecked")
    private static final ResourceKey<EquipmentAsset> ASSET_KEY = ResourceKey.create(
            (ResourceKey) EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(MODID, "thigh_highs"));

    private static CustomData defaultData()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("base_color", 0xFFFFFF);
        tag.putInt("stripe_color", 0xFFFFFF);
        return CustomData.of(tag);
    }

    public static int getBaseColor(ItemStack stack)
    {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null)
            return customData.copyTag().getIntOr("base_color", 0xFFFFFF);
        return 0xFFFFFF;
    }

    public static int getStripeColor(ItemStack stack)
    {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null)
            return customData.copyTag().getIntOr("stripe_color", 0xFFFFFF);
        return 0xFFFFFF;
    }

    public static int mixColor(int existing, int newDye)
    {
        int r1 = (existing >> 16) & 0xFF, g1 = (existing >> 8) & 0xFF, b1 = existing & 0xFF;
        int r2 = (newDye   >> 16) & 0xFF, g2 = (newDye   >> 8) & 0xFF, b2 = newDye   & 0xFF;

        float avgR = (r1 + r2) / 2.0f;
        float avgG = (g1 + g2) / 2.0f;
        float avgB = (b1 + b2) / 2.0f;

        // Average the per-color brightness (each color's own max channel), not the global max.
        float avgMax = (Math.max(r1, Math.max(g1, b1)) + Math.max(r2, Math.max(g2, b2))) / 2.0f;
        float curMax = Math.max(avgR, Math.max(avgG, avgB));

        if (curMax == 0.0f) return 0;

        float factor = avgMax / curMax;
        int finalR = Math.min(255, (int)(avgR * factor));
        int finalG = Math.min(255, (int)(avgG * factor));
        int finalB = Math.min(255, (int)(avgB * factor));

        return (finalR << 16) | (finalG << 8) | finalB;
    }

    public static ItemStack setColors(ItemStack stack, int baseColor, int stripeColor)
    {
        ItemStack result = stack.copy();
        CustomData existing = result.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt("base_color", baseColor);
        tag.putInt("stripe_color", stripeColor);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return result;
    }

    public static final DeferredItem<ThighHighsItem> THIGH_HIGHS =
            LittleContentRegistry.ITEMS.registerItem("thigh_highs",
                    props -> new ThighHighsItem(props
                            .durability(65)
                            .enchantable(15)
                            .repairable(LittleMaterials.FABRIC_TAG)
                            .component(DataComponents.CUSTOM_DATA, defaultData())
                            .component(DataComponents.EQUIPPABLE,
                                    Equippable.builder(EquipmentSlot.FEET)
                                            .setAsset(ASSET_KEY)
                                            .build())
                    ));

    public static void register()
    {
        // Trigger static initializer — THIGH_HIGHS is registered on field access.
        // Also register the dye recipe serializer.
        ThighHighsDyeRecipe.registerSerializer();
    }
}
