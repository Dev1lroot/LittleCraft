/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.item.BabyBottle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

public class BabyBottleFillRecipe extends CustomRecipe
{
    private final String           fluidName;
    private final int              fluidAmount;
    private final List<Ingredient> ingredients;

    public BabyBottleFillRecipe(String fluidName, int fluidAmount, List<Ingredient> ingredients)
    {
        this.fluidName   = fluidName;
        this.fluidAmount = fluidAmount;
        this.ingredients = ingredients;
    }

    public static final MapCodec<BabyBottleFillRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    Codec.STRING.fieldOf("fluid_name").forGetter(r -> r.fluidName),
                    Codec.INT.fieldOf("fluid_amount").forGetter(r -> r.fluidAmount),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.ingredients)
            ).apply(inst, BabyBottleFillRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BabyBottleFillRecipe> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public BabyBottleFillRecipe decode(RegistryFriendlyByteBuf buf)
                {
                    String fluidName   = buf.readUtf();
                    int    fluidAmount = buf.readVarInt();
                    int    count       = buf.readVarInt();
                    List<Ingredient> ingredients = new ArrayList<>(count);
                    for (int i = 0; i < count; i++)
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    return new BabyBottleFillRecipe(fluidName, fluidAmount, ingredients);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BabyBottleFillRecipe recipe)
                {
                    buf.writeUtf(recipe.fluidName);
                    buf.writeVarInt(recipe.fluidAmount);
                    buf.writeVarInt(recipe.ingredients.size());
                    for (Ingredient ingredient : recipe.ingredients)
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                }
            };

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BabyBottleFillRecipe>> SERIALIZER =
            LittleContentRegistry.RECIPE_SERIALIZERS.register("baby_bottle_fill",
                    () -> new RecipeSerializer<>(CODEC, STREAM_CODEC));

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) items.add(stack);
        }

        // Must be exactly 1 bottle + the specified ingredients
        if (items.size() != ingredients.size() + 1) return false;

        // Find an empty bottle in the grid
        List<ItemStack> remaining = new ArrayList<>(items);
        boolean foundBottle = false;
        for (int i = 0; i < remaining.size(); i++)
        {
            ItemStack stack = remaining.get(i);
            if (stack.getItem() instanceof BabyBottle.BabyBottleItem
                    && BabyBottle.getFluidAmount(stack) == 0)
            {
                remaining.remove(i);
                foundBottle = true;
                break;
            }
        }
        if (!foundBottle) return false;

        // Match remaining grid items against ingredients (each consumed once)
        List<Ingredient> unmatched = new ArrayList<>(ingredients);
        for (ItemStack stack : remaining)
        {
            boolean matched = false;
            for (int j = 0; j < unmatched.size(); j++)
            {
                if (unmatched.get(j).test(stack))
                {
                    unmatched.remove(j);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        return unmatched.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof BabyBottle.BabyBottleItem
                    && BabyBottle.getFluidAmount(stack) == 0)
            {
                return BabyBottle.setFluid(stack, fluidName, fluidAmount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<BabyBottleFillRecipe> getSerializer()
    {
        return SERIALIZER.get();
    }

    public static void registerSerializer() {}
}
