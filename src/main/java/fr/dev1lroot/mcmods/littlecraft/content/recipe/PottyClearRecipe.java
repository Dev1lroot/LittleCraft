/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.recipe;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.block.PottyBlock;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PottyClearRecipe extends CustomRecipe
{
    public static final MapCodec<PottyClearRecipe> CODEC = MapCodec.unit(PottyClearRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PottyClearRecipe> STREAM_CODEC =
        StreamCodec.of(
            (buf, recipe) -> {},
            buf -> new PottyClearRecipe()
        );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PottyClearRecipe>> SERIALIZER =
        LittleContentRegistry.RECIPE_SERIALIZERS.register("potty_clear",
            () -> new RecipeSerializer<>(CODEC, STREAM_CODEC));

    public PottyClearRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        int pottyCount = 0;
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (isPotty(stack) && stack.has(DataComponents.BLOCK_ENTITY_DATA))
                pottyCount++;
            else
                return false;
        }
        return pottyCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && isPotty(stack))
            {
                ItemStack result = stack.copyWithCount(1);
                result.remove(DataComponents.BLOCK_ENTITY_DATA);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<PottyClearRecipe> getSerializer()
    {
        return SERIALIZER.get();
    }

    private static boolean isPotty(ItemStack stack)
    {
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof PottyBlock;
    }

    public static void registerSerializer() {}
}
