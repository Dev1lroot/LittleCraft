/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.recipe;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.item.ThighHighs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

public class ThighHighsDyeRecipe extends CustomRecipe
{
    public static final MapCodec<ThighHighsDyeRecipe> CODEC =
            MapCodec.unit(ThighHighsDyeRecipe::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, ThighHighsDyeRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> { /* stateless — nothing to write */ },
                buf -> new ThighHighsDyeRecipe()
            );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ThighHighsDyeRecipe>> SERIALIZER =
            LittleContentRegistry.RECIPE_SERIALIZERS.register("thigh_highs_dye",
                    () -> new RecipeSerializer<>(CODEC, STREAM_CODEC));

    public ThighHighsDyeRecipe()
    {
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        int thighHighsCount = 0;
        int dyeCount = 0;

        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof ThighHighs.ThighHighsItem)
                thighHighsCount++;
            else if (stack.getItem() instanceof DyeItem)
                dyeCount++;
            else
                return false; // unexpected item
        }

        return thighHighsCount == 1 && (dyeCount == 1 || dyeCount == 2);
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        ItemStack thighHighsStack = ItemStack.EMPTY;
        List<DyeColor> dyeColors = new ArrayList<>();

        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof ThighHighs.ThighHighsItem)
            {
                thighHighsStack = stack;
            }
            else if (stack.getItem() instanceof DyeItem)
            {
                DyeColor color = stack.get(DataComponents.DYE);
                if (color != null)
                    dyeColors.add(color);
            }
        }

        if (thighHighsStack.isEmpty()) return ItemStack.EMPTY;

        int existingBase   = ThighHighs.getBaseColor(thighHighsStack);
        int existingStripe = ThighHighs.getStripeColor(thighHighsStack);

        int newBase;
        int newStripe;

        if (dyeColors.size() == 1)
        {
            int dyeRgb = dyeColors.get(0).getTextureDiffuseColor() & 0x00FFFFFF;
            newBase   = ThighHighs.mixColor(existingBase,   dyeRgb);
            newStripe = ThighHighs.mixColor(existingStripe, dyeRgb);
        }
        else if (dyeColors.size() == 2)
        {
            int baseRgb   = dyeColors.get(0).getTextureDiffuseColor() & 0x00FFFFFF;
            int stripeRgb = dyeColors.get(1).getTextureDiffuseColor() & 0x00FFFFFF;
            newBase   = ThighHighs.mixColor(existingBase,   baseRgb);
            newStripe = ThighHighs.mixColor(existingStripe, stripeRgb);
        }
        else
        {
            return ItemStack.EMPTY;
        }

        return ThighHighs.setColors(thighHighsStack, newBase, newStripe);
    }

    @Override
    public RecipeSerializer<ThighHighsDyeRecipe> getSerializer()
    {
        return SERIALIZER.get();
    }

    public static void registerSerializer()
    {
        // Trigger static initializer — RECIPE_SERIALIZERS and SERIALIZER fields are initialized on access.
        // The actual bus registration happens in LittleContentRegistry.register().
    }
}
