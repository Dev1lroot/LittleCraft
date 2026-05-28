/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.recipe;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import fr.dev1lroot.mcmods.littlecraft.content.item.LittleMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DiaperPaddingRecipe extends CustomRecipe
{
    public static final MapCodec<DiaperPaddingRecipe> CODEC =
            MapCodec.unit(DiaperPaddingRecipe::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, DiaperPaddingRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> { /* stateless */ },
                buf -> new DiaperPaddingRecipe()
            );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DiaperPaddingRecipe>> SERIALIZER =
            LittleContentRegistry.RECIPE_SERIALIZERS.register("diaper_padding",
                    () -> new RecipeSerializer<>(CODEC, STREAM_CODEC));

    public DiaperPaddingRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        int diaperCount  = 0;
        int paddingCount = 0;

        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof Diaper.DiaperItem)
                diaperCount++;
            else if (stack.getItem() == LittleMaterials.PADDING.get())
                paddingCount++;
            else
                return false;
        }

        return diaperCount == 1 && paddingCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        ItemStack diaperStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof Diaper.DiaperItem)
            {
                diaperStack = stack;
                break;
            }
        }

        if (diaperStack.isEmpty()) return ItemStack.EMPTY;

        int newCapacity = Math.min(Diaper.getCapacity(diaperStack) + 1000, Diaper.MAX_CAPACITY);

        ItemStack result = diaperStack.copy();
        CustomData existing = result.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt("capacity", newCapacity);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        result.setCount(1);
        return result;
    }

    @Override
    public RecipeSerializer<DiaperPaddingRecipe> getSerializer()
    {
        return SERIALIZER.get();
    }

    public static void registerSerializer() {}
}
